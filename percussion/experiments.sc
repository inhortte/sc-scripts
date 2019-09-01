s.boot;

s.plotTree;

q = { Pan2.ar(WhiteNoise.ar(Line.kr(0.2, 0, 2)), Line.kr(-1, 1, 2, doneAction: Done.freeSelf)) }.play;
q.free;

(
  SynthDef(\pluckDeath, {
    arg freq = 440, amp;
    var quart = freq / 4;
    var pluck = 
      Pluck.ar(
        WhiteNoise.ar(amp).dup(2),
        Env.perc.kr(2),
        quart.reciprocal, freq.reciprocal, -1, Rand(0.01, 0.2), mul: 1
      );
    var filter = HPF.ar(
      in: pluck,
      freq: Line.kr(freq, freq * 2, 1, doneAction: Done.freeSelf)
    );
    Out.ar(0,
      Pan2.ar(
        filter,
        [ -0.5, 0.5 ]
        // Array.fill(2, { Rand(-1.0, 1.0) })
      )
    )
  }).add;
)
Synth(\pluckDeath, [\amp, 1, \freq, 220]);
(
  var seq = [ 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 2960, 1396, 1396, 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1396, 2960, 1396, 1396, 
  ];
  Pbind(
    \instrument, \pluckDeath,
    \dur, Pseq([0.333333, 0.333333, 1.333333], inf),
    \amp, Pseq([0, 0.5, 0.4], inf),
    \freq, Pseq(seq, 1)
  ).play(TempoClock(72/60));
)

(
  SynthDef(\dustOfTime, { 
    arg density = 512, dur = 2, amp = 0.5;
    var dustEnv = XLine.kr(start: density, end: 12, dur: dur * 2, doneAction: Done.freeSelf) ! 4;
    var dustAmp = Array.fill(4, amp).postln;
    var dust = Dust.ar(density: dustEnv, mul: dustAmp);
    Out.ar(
      0,
      Splay.ar(dust);
    );
  }).add;
  SynthDef(\pulseOfAgatha, {
    arg freq = 220, amp = 0.5, release = 0.1, mod = 1;
    var pulseAmp = amp;
    var pulseEnv = Env.perc(attackTime: 0.001, releaseTime: release, level: pulseAmp, curve: -0.2).kr(2);
    var pulseDur = mod / 16;
    var pulse = Pulse.ar(
      freq: freq,
      width: [ Line.kr(0.1, 0.3, pulseDur), Line.kr(0.9, 0.7, pulseDur) ]
    ) * pulseEnv;
    Out.ar(
      0,
      pulse
    );
  }).add;
)

Synth(\dustOfTime, [\dur, 1]);
Synth(\pulseOfAgatha)
(
  var clock = TempoClock(72/60);
  var ampSeq = [ 
    0.7, 0.4, 0, 0.4, 
    0.4, 0, 0, 0,
    0, 0, 0.5, 0,
    0, 0, 0, 0
  ];
  var releaseSeq = Array.fill(10, 0.15) ++ [ 1 ] ++ Array.fill(5, 0);
  var dustSeq = [0.5] ++ Array.fill(9, 0) ++ [0.5] ++ Array.fill(5, 0);
  releaseSeq.postln;
  Pbind(
    \instrument, \pulseOfAgatha,
    \amp, Pseq(ampSeq, inf),
    \release, Pseq(releaseSeq, inf),
    \mod, 2,
    \dur, 1/4,
    \freq, 73
  ).play(clock);
  Pbind(
    \instrument, \dustOfTime,
    \amp, dustSeq,
    \dur, 1/4
  ).play(clock);
)

