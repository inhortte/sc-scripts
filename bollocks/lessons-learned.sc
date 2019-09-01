s.boot;
s.plotTree;

(
  {
    var clock = TempoClock(72/60), dust, pulseEnv, ampSeq, control;
    ~cBus = Bus.control(s, 1);
    pulseEnv = XLine.kr(0.5, 0.2, dur: 2, doneAction: 2);
    ampSeq = [1, 0, 0, 0, 0, 1, 0, 0]; // in eighths
    control = {
      Out.kr(
        ~cBus,
        Pulse.kr(
          freq: 512, mul: pulseEnv
        )
      )
    }.play(clock);
    /*
    dust = Dust2.ar(In.kr(~cBus));
    Out(0, 
      Pan2(dust, 0)
    );
    */
  }.play;
)

(
  SynthDef(\dustOfTime, {
    arg density = 512, duration = 2, amp = 0.5;
    var durs = duration * [0.8, 0.9, 1.1, 1.2];
    var densityEnv = XLine.kr(density, 12, durs, mul: amp, doneAction: Done.freeSelf);
    var dust = Dust2.ar(densityEnv);
    Out.ar(
      0,
      Splay.ar(dust)
    )
  }).add;
)
Synth(\dustOfTime);
(
  SynthDef(\marrow, {
    var durs = 2 * [0.8, 0.9, 1.1, 1.2];
    var dust = Dust.ar(XLine.kr(512, 12, durs, doneAction: Done.freeSelf));
    Out.ar(0, Splay.ar(dust));
  }).add;
)
Synth(\marrow);
(
  var clock = TempoClock(72/60);
  var ampSeq = [1, 0, 0, 0, 0, 1, 0, 0]; // in eighths
  var durationSeq = [0.5, 0, 0, 0, 0, 1.7, 0, 0];
  Pbind(
    \instrument, \dustOfTime,
    \amp, Pseq(ampSeq, inf),
    \duration, Pseq(durationSeq, inf),
    \dur, 1/2
  ).play(clock);
)

(
  SynthDef(\muermo, {
    arg freq = 220;
    var saw = Saw.ar(freq, 0.1);
    Out.ar(0, Pan2.ar(saw, 0));
  }).add;
)
g = Synth(\muermo);
g.free;
g.set(\freq, ~busOne.asMap);
g.set(\freq, ~busTwo.asMap);
(
  ~busOne = Bus.control(s, 1);
  ~busTwo = Bus.control(s, 2);
  // {LFSaw.kr(0.5, add: 219.5)}.plot;
  {Out.kr(~busOne, LFSaw.kr(0.5, add: 219.5))}.play;
  {Out.kr(~busTwo, LFSaw.kr(1).range(220, 440))}.play;
)

(
  ~fxBus = Bus.audio(s, 1);
  ~masterBus = Bus.audio(s, 1);
  SynthDef(\noise, {
    var white = WhiteNoise.ar(0.2);
    Out.ar(~fxBus, white);
  }).add;
  SynthDef(\filter, {
    var band = BPF.ar(
      in: In.ar(~fxBus),
      freq: MouseY.kr(500, 3000),
      rq: 0.1
    );
    Out.ar(
      ~masterBus, band
    )
  }).add;
  SynthDef(\master, {
    arg amp = 0.5;
    Out.ar(0, Pan2.ar(In.ar(~masterBus) * Lag.kr(amp, 1), 0));
  }).add;
)

a = Synth(\master);
b = Synth(\filter);
c = Synth(\noise);

a.set(\amp, 0.1);
