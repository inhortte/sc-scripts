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
(
  var durs = [ 1.7777778, 1.5555556 ];
  var freqs = [ 
    43.65, 43.65, 43.65, 43.65, // f
    43.65, 43.65, 43.65, 43.65, // f
    69.3, 69.3, 69.3, 69.3, // c#
    69.3, 69.3, 69.3, 69.3, // c#
    61.74, 61.74, 61.74, 61.74, // b
    58.27, 58.27 // Bb
  ];
  Pbind(
    \instrument, \tamborLow,
    \freq, Pseq(freqs, 1),
    \dur, Pseq(durs, inf),
    \amp, 0.5
  ).play;
)

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
    var brown = WhiteNoise.ar(brownEnv) * amp;
    var bpf = BPF.ar(
      in: brown,
      freq: freq,
      rq: LFTri.kr(1 / cycle).range(0.1, 4)
    );
    var lpf = LPF.ar(bpf, freq * 8);
    var aPass = AllpassL.ar(
      in: lpf,
      maxdelaytime: 0.03125,
      delaytime: [ 0, 0.015625, 0.0234375, 0.01953125, 0.015 ],
      decaytime: 0.015625
    );
    Out.ar(outBus, Splay.ar(aPass));
  }).add;
)
Synth(\breathingDust, [ cycle: 4, amp: 0.4 ]);

Array.fill(10, { 1.0.linrand });
Array.fill(16, { 1.0 - 2.0.rand });
(
  SynthDef(\rakingPulse, {
    arg outBus = 0, freq = 1396.91, amp = 0.1, rq1 = 0.5, rq2 = 0.5, grainTrigger = 0, grainDur = 1, grainPan = 0;
    var baseNoise = BrownNoise.ar(amp);
    var lpf1 = RLPF.ar(in: baseNoise, freq: freq, rq: rq1);
    var lpf2 = RLPF.ar(in: baseNoise, freq: freq * 0.97, rq: rq2);
    var grainy = GrainIn.ar(2, grainTrigger, grainDur, [ lpf1, lpf2 ], grainPan, -1, 64);
    var aPass = AllpassL.ar(
      in: grainy,
      maxdelaytime: 0.03125,
      delaytime: [ 0, 0.015625, 0.0234375, 0.01953125, 0.015 ],
      decaytime: [ 0.015625, 0.2343, 0.019 ]
    );
    Out.ar(outBus, Splay.ar(aPass)); 
  }).add;
)
(
  SynthDef(\crackling, {
    arg outBus = 0, freq = 1396.91, amp = 0.1, rq1 = 0.5, rq2 = 0.5, chaos = 1.5, dur = 1;
    var env = EnvGen.ar(
      Env.circle([ freq * 4, freq / 8 ], [ dur * 0.8, dur * 0.2 ], [ 2, -5 ]),
      doneAction: Done.none
    );
    var whiteNoise = Crackle.ar(chaos, amp);
    var dist = InsideOut.ar(whiteNoise);
    var bpf1 = BPF.ar(in: dist, freq: freq, rq: rq1 * 0.25);
    var bpf2 = BPF.ar(in: dist, freq: freq * 0.94, rq: rq2 * 0.25);
    var lpf = LPF.ar([ bpf1, bpf2 ], freq: env);
    Out.ar(outBus, Splay.ar(lpf));
  }).add;
)
Synth(\rakingPulse);
Synth(\crackling, [ amp: 0.9, chaos: 2.0 ]);
(
  var rqs = Array.fill(10, { 1.5.linrand });
  var pans = Array.fill(16, { 1.0 - 2.0.rand });
  var beatLength = 0.6666666666666666, swing = 0.6666666666666;
  var rp = Synth(\rakingPulse, [ grainDur: beatLength / 36, amp: 0.5 ]);
  var cr = Synth(\crackling, [ amp: 1.3, chaos: 2.0, dur: beatLength ]);
  Routine({
    loop({
      rqs.do({
        rp.set(\rq1, rqs.choose);
        cr.set(\rq1, rqs.choose);
        rp.set(\rq2, rqs.choose);
        cr.set(\rq2, rqs.choose);
        rp.set(\grainPan, pans.choose);
        (beatLength * swing).wait;
        rp.set(\rq1, rqs.choose);
        rp.set(\rq2, rqs.choose);
        rp.set(\grainPan, pans.choose);
        (beatLength - (beatLength * swing)).wait;
      });
    })
  }).play;
  Routine({
    loop({
      rp.set(\grainTrigger, 1);
      0.001.wait;
      rp.set(\grainTrigger, 0);
      (beatLength / 24 - 0.001).wait;
    });
  }).play;
)

Array.fill(6, { 1 + 2.0.sum3rand });

(
  SynthDef(\pulsePerc, {
    arg outBus = 0, freq = 1396.91, amp = 0.1, cycle = 1;
    var circleEnv = EnvGen.kr(
      Env.circle(levels: [0.3, 1], times: [ cycle * 0.99, cycle * 0.01 ], curve: [ -5, 0 ]),
      doneAction: Done.none
    );
    var dust = Dust.ar(
      circleEnv * 512 * Array.fill(6, { 1 + 2.0.sum3rand })
    );
    var brownEnv = EnvGen.ar(
      Env.perc(0.0001, 0.0001, curve: -5),
      dust,
      doneAction: Done.none
    );
    var noise = PinkNoise.ar(brownEnv) * amp;
    var bpf = BPF.ar(
      in: noise,
      freq: [ freq * 2, freq ],
      rq: LFTri.kr(1 / cycle).range(0.4, 2)
    );
    var aPass = AllpassL.ar(
      in: bpf,
      maxdelaytime: 0.03125,
      delaytime: [ 0, 0.015625, 0.0234375 ],
      decaytime: [ 0.015625, 0.015 ]
    );
    Out.ar(outBus, Splay.ar(aPass));
  }).add;
)
(
  SynthDef(\thwizzle, {
    arg outBus = 0, inBus = 7, cycle = 1, freq = 1396.91, multiplier = 4;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.ar([cycle * 2, cycle, cycle * 0.333333, cycle * 0.1111111]).range(freq * 0.6 * multiplier, freq * 1.4 * multiplier),
      rq: 0.7
    );
    Out.ar(outBus, Splay.ar(lpf));
  }).add;
)
(
  g = Group.basicNew(s, 1);
  d = Bus.audio(s, 2);
  Synth.head(g, \pulsePerc, [ outBus: d, cycle: 0.66666666, amp: 0.8 ]);
  Synth.tail(g, \thwizzle, [ inBus: d, cycle: 5.333333, multiplier: 4 ]);
)
