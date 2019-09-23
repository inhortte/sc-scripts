(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

// Bass drum
(
  SynthDef(\thomp, {
    arg outBus = 0, freq = 52, ratio = 3, sweeptime = 0.05, preamp = 1, amp = 0.1, decay1L = 0.8, dur = 1, decay1Base = 0.3, decay2Base = 0.15;
    var decay1 = decay1Base * dur, decay2 = decay2Base * dur;
    var fEnv = EnvGen.kr(
      Env.new([ freq * ratio, freq ], [ sweeptime ], \exp)
    );
    var env = EnvGen.kr(
      Env.new([0, 1, decay1L, 0], [0.01, decay1, decay2], -3),
      doneAction: Done.freeSelf
    );
    var signal = SinOsc.ar(fEnv, 0.5pi, preamp).distort * env * amp;
    Out.ar(outBus, signal ! 2);
  }).add;
)

// high hat
(
  SynthDef(\snare, {
    arg outBus = 0, freq = 1661, rq = 3, decay = 0.4, pan = 0, amp = 0.1;
    var env = Env.perc(0.01, decay).kr(2);
    var signal = PinkNoise.ar(amp);
    var filter = BPF.ar(signal, freq, rq) * env;
    Out.ar(outBus, Pan2.ar(filter, pan));
  }).add;
)

Synth(\thomp, [ freq: 52, amp: 0.5, decay1Base: 0.1, decay2Base: 0.2, ratio: 4, sweeptime: 0.08 ]);
Synth(\snare, [ freq: 3729, amp: 12, decay: 0.04, rq: 0.06 ]);
(
  ~hhAmps = Array.fill(512, {
    arg n;
    var baseAmp = 17;
    var leper = case
      { n.mod(5) == 0 } { 0 }
      { n.mod(7) == 0 } { 0 }
      { n.mod(3) == 0 } { baseAmp * 0.7 }
      { n == n } { baseAmp };
    leper;
  });
)
(
  var clock = TempoClock(84/60);
  var thompDurs = [1, 1, 0.5, 1.5];
  var thompAmps = [0.5, 0.4, 0.4, 0.3];
  Pbind(
    \instrument, \thomp,
    \dur, Pseq(thompDurs, inf),
    \amp, Pseq(thompAmps, inf),
    \freq, 52,
    \decay1Base, 0.05,
    \decay2Base, 0.1
  ).play(clock);
  Pbind(
    \instrument, \snare,
    \amp, Pseq(~hhAmps, 1),
    \pan, Prand([-0.8, -0.3, 0, 0.3, 0.8 ], inf),
    \dur, 0.5,
    \freq, 6645,
    \decay, 0.04,
    \rq, 0.04
  ).play(clock);
)
