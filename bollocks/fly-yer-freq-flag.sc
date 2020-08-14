(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

20.do { 5.rand.postln; }

(
  SynthDef(\tamborLow, {
    arg outBus = 0, freq = 43.65, dur = 1, amp = 0.1;
    var clickEnv = Env.perc(0.001, 0.001, curve: -3).kr(0);
    var tri1Env = Env.perc(0.05, dur * 0.3, curve: -4).kr(0);
    var membraneEnv = Env.perc(0.01, dur * 0.2, curve: -2).kr(0);
    var dustEnv = Env.perc(0.001, dur * 1.2, curve: 4).kr(2);
    var click = WhiteNoise.ar(amp * 0.1) * clickEnv;
    var tri1 = LFTri.ar(
      freq: [ freq, freq * 2 + 3, freq * 4 + 9, freq * 8 + 43 ],
      iphase: [ 0, 1, 2, 3 ],
      mul: [ amp, amp * 0.6, amp * 0.2, amp * 0.1 ]
    ) * tri1Env;
    var fmGrains = FMGrain.ar(
      [ Dust.ar(12, dustEnv), Dust.ar(3, dustEnv), Dust.kr(12, dustEnv), Dust.kr(3, dustEnv) ],
      0.05,
      carfreq: freq * 5,
      modfreq: freq * 12,
      index: LFNoise1.kr(1).range(1, 10),
      mul: dustEnv * amp * 0.1
    );
    var membrane = MembraneCircle.ar(
      PinkNoise.ar(amp * 0.2),
      0.02,
      0.9999
    ) * membraneEnv;
    var mix = Mix.new([ [ click, click, click, click ], tri1, [ membrane, membrane, membrane, membrane ], fmGrains ]);
    Out.ar(outBus, Splay.ar(mix));
  }).add;
)
Synth(\tamborLow, [ dur: 1, amp: 0.5 ]);

Env.circle(levels: [1, 0.2, 1], times: [ 4, 4 ], curve: [ 1, -1 ]).plot;
(
  SynthDef(\breathingDust, {
    arg outBus = 0, freq = 698.46, cycle = 0.1, amp = 0.1;
    var circleEnv = EnvGen.kr(
      Env.circle(levels: [1, 0.05, 1], times: [ cycle * 0.5, cycle * 0.5, 0 ], curve: [ 1, -1, 0 ]),
      doneAction: Done.none
    );
    var dust = Dust2.ar(
      circleEnv * 1024
    );
    var brownEnv = EnvGen.ar(
      Env.perc(0.0001, 0.0005, curve: -5),
      dust,
      doneAction: Done.none
    );
    var brown = ClipNoise.ar(brownEnv) * amp;
    var bpf = BPF.ar(
      in: brown,
      freq: freq,
      rq: LFTri.kr(1 / cycle).range(0.1, 4)
    );
    var aPass = AllpassL.ar(
      in: bpf,
      maxdelaytime: 0.03125,
      delaytime: [ 0, 0.015625, 0.0234375, 0.01953125, 0.015 ],
      decaytime: 0.015625
    );
    Out.ar(outBus, Splay.ar(aPass));
  }).add;
)
Synth(\breathingDust, [ cycle: 4, amp: 0.4 ]);
