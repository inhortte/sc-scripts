(
  s.options.numBuffers = 8192; 
  s.boot;
  s.plotTree;
)
(
  SynthDef(\aFewSines, { | freq = 164, sustain = 3, harmonics, amp |
    var env = EnvGen.ar(
      Env.linen(attackTime: 0.01, sustainTime: sustain, releaseTime: 1.0, level: amp, curve: -0.9),
      gate: 1.0,
      doneAction: Done.freeSelf
    );
    var sine = SinOscFB.ar(
      freq: freq * harmonics,
      feedback: Line.kr(2 * pi / 3, pi / 8, sustain),
      mul: 0.5
    ) * env;
    var hpf = HPF.ar(
      in: sine,
      freq: Line.kr(freq, freq * 24, sustain)
    );
    Out.ar(0, Splay.ar(hpf)); 
  }).add;
)
(
  SynthDef(\serratedMadness, { | freq = 164, sustain = 3, amp = 0.3 |
    var env = EnvGen.ar(
      Env.linen(attackTime: 0.01, sustainTime: sustain, releaseTime: 1.0, level: amp, curve: 0.9),
      gate: 1.0,
      doneAction: Done.freeSelf
    );
    var eSaw = LFSaw.ar(
      freq: 164,
      iphase: [0.4, 0.9, 1.7],
      mul: 0.3
    ) * env;
    var hpf = HPF.ar(
      in: eSaw,
      freq: Line.kr(freq, freq * 24, sustain)
    );
    Out.ar(
      0,
      Splay.ar(hpf)
    )
  }).add;
)
Synth(\aFewSines, [ sustain: 10, harmonics: [3, 5, 7], amp: [0.6, 0.3, 0.1] ]);
(
  Synth(\aFewSines, [ sustain: 60, harmonics: [3, 5, 7], amp: [0.6, 0.3, 0.1] ]);
  Synth(\serratedMadness, [ freq: 164, sustain: 60, amp: 0.4 ]);
  Synth(\serratedMadness, [ freq: 110, sustain: 60, amp: 0.4 ]);
)

g.free
