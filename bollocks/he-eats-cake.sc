(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\midKlang, {
    arg outBus = 0, freq = 120, dur = 1, amp = 0.1;
    var klangEnv = Env.perc(0.01, dur * 0.8, curve: -5).kr(2);
    var springEnv = Env.perc(0.01, dur * 0.1, curve: -2).kr(0);
    var klang = Klang.ar(
      `[[ freq, freq * 2, freq * 3.7 ], [ amp, amp * 0.7, amp * 0.7 ], [ pi, pi, pi ]], 
      1, 0
    ) * klangEnv;
    var dust = Dust.ar(2);
    var spring = ClipNoise.ar([amp * 0.6, amp * 0.74] * 0.25) * springEnv;
    var out = AllpassN.ar(klang, 0.1, [0.1.rand, 0.1.rand], 9);
    var mix;
    out = AllpassN.ar(out, 0.1, [0.1.rand, 0.1.rand], -3);
    mix = Mix([ out, spring ]);
    Out.ar(outBus, mix);
  }).add;
)
Synth(\midKlang, [ freq: 120, dur: 1, amp: 0.1 ]);
(
  var clock = TempoClock(72/60);
  var pBus = Bus.audio(s, 2);
  var freqs = [
    \, 165, 196, 185, 165, 131, 139, 165, 220, 262, 247, 220,
    165, 196, 185, 165, 131, 139, 165, 220, 262, 247, 220,
    185, 220, 196, 185, 156, 165, 185, 247, 294, 262, 247,
    185, 220, 196, 185, 156, 165, 185, 247, 294, 262, 247,
    196, 247, 220, 196, 147, 165, 196, 294, 330, 294, 277,
    196, 247, 220, 196, 147, 165, 196, 294, 330, 294, 277,
    220, 277, 247, 220, 311, 330
  ] / 2;
  var durs = [
    0.5, 0.5, 0.25, 1.25, 0.5, 0.25, 1.25, 0.5, 0.1666667, 0.1666667, 0.1666667, 1,
    0.5, 0.25, 1.25, 0.5, 0.25, 1.25, 0.5, 0.1666667, 0.1666667, 0.1666667, 1,
    0.5, 0.25, 1.25, 0.5, 0.25, 1.25, 0.5, 0.1666667, 0.1666667, 0.1666667, 1,
    0.5, 0.25, 1.25, 0.5, 0.25, 1.25, 0.5, 0.1666667, 0.1666667, 0.1666667, 1,
    0.5, 0.25, 1.25, 0.5, 0.25, 1.25, 0.5, 0.1666667, 0.1666667, 0.1666667, 1,
    0.5, 0.25, 1.25, 0.5, 0.25, 1.25, 0.5, 0.1666667, 0.1666667, 0.1666667, 1,
    0.5, 0.25, 1.25, 0.5, 0.25, 2.25
  ];
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \midKlang,
    \group, g,
    \addAction, 0,
    \outBus, 0,
    \dur, Pseq(durs, inf),
    \freq, Pseq(freqs, 1),
    \amp, Prand([0.2, 0.18, 0.22, 0.19, 0.21], inf)
  ).play(clock);
)
