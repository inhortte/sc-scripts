(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\lowpong, { | tension = 0.1, amp = 0.7 |
    var noise = BrownNoise.ar(0.4);
    var env = EnvGen.kr(
      Env.perc,
      1,
      // MouseButton.kr(0, 1, 0),
      timeScale: [0.15, 0.2, 0.13],
      doneAction: Done.freeSelf
    );
    var loss = 0.99;
    var mc = MembraneCircle.ar(env * noise, tension, loss, mul: amp);
    Out.ar(0, Splay.ar(mc));
  }).add;
)
(
  var baseAmps = Array.fill(64, {1}).postln;
  var sined = baseAmps.collect({ 
    arg amp, idx;
    (amp * idx.sin(pi * idx));
  }).normalize(0, 0.3).scramble;
  var cosd = baseAmps.collect({
    arg amp, idx;
    (amp * idx.cos(pi * idx));
  }).normalize(0.1, 0.3).scramble;
  var tand = baseAmps.collect({
    arg amp, idx;
    (amp * idx.tan(pi * idx));
  }).normalize(0, 0.3).scramble;
  var arctand = baseAmps.collect({
    arg amp, idx;
    (amp * idx.atan(pi * idx));
  }).normalize(0.005, 0.03).scramble;
  var dur1 = [ 1/2, 1/4, 1/2, 1/4, 1/2, 1/4 ].scramble;
  var dur2 = [ 1/2, 1/4, 1/2, 1/4, 1/2, 1/4, 1/2, 1/4, 1/2, 1/4, 1/2, 1/4, 1/2, 1/4 ].scramble;

  var sinPattern = Pbind(
    \instrument, \lowpong,
    \freq1, 55,
    \freq2, 58.27,
    \dur, Pseq(dur1, inf),
    \amp, Pseq(sined, inf) * 0.3,
    \tension, Pseq(cosd, inf)
  );
  var tanPattern = Pbind(
    \instrument, \lowpong,
    \freq1, 49,
    \freq2, 55,
    \dur, Pseq(dur2, inf),
    \amp, Pseq(tand, inf) * 0.2,
    \tension, Pseq(arctand, inf)
  );

  Ppar([sinPattern, tanPattern], inf).play(TempoClock(72/60));
)
(
  SynthDef(\bassgrowl, { | freq1 = 55, freq2 = 58, cutoffLow = 100, cutoffHigh = 1024, dur = 1 |
    var thurk;
    thurk = RLPF.ar(
      in: LFTri.ar(
        freq: [freq1, freq2],
        iphase: [0, 1.3],
        mul: EnvGen.kr(
          Env.perc(attackTime: 0.1, releaseTime: dur * 0.2, curve: -2.7),
          1.0,
          doneAction: Done.freeSelf
        ) * 0.2
      ),
      freq: LinExp.kr(
        Line.kr(0.1, 0.9, 0.15),
        -1,
        1,
        cutoffLow,
        cutoffHigh
      ),
      rq: SinOsc.kr(0.4).range(0.1, 0.5),
      mul: 0.5,
      add: 0.0
    );
    Out.ar(0, thurk);
  }).add;
)

(
  var baseAmps = Array.fill(64, {1}).postln;
  var sined = baseAmps.collect({ 
    arg amp, idx;
    (amp * idx.sin(pi * idx));
  }).normalize(0, 0.3).scramble;
  var cosd = baseAmps.collect({
    arg amp, idx;
    (amp * idx.cos(pi * idx));
  }).normalize(500, 2000).scramble;
  var tand = baseAmps.collect({
    arg amp, idx;
    (amp * idx.tan(pi * idx));
  }).normalize(0, 0.3).scramble;
  var arctand = baseAmps.collect({
    arg amp, idx;
    (amp * idx.atan(pi * idx));
  }).normalize(500, 2000).scramble;

  var sinPattern = Pbind(
    \instrument, \bassgrowl,
    \freq1, 55,
    \freq2, 58.27,
    \dur, 1/4,
    \amp, Pseq(sined, inf) * 0.25,
    \cutoffHigh, Pseq(cosd, inf)
  );
  var tanPattern = Pbind(
    \instrument, \bassgrowl,
    \freq1, 49,
    \freq2, 55,
    \dur, 1/2,
    \amp, Pseq(tand, inf) * 0.25,
    \cutoffHigh, Pseq(arctand, inf)
  );

  Ppar([sinPattern, tanPattern], inf).play(TempoClock(72/60));
)

(
  SynthDef(\sawish, {
    arg outBus = 0, freq = 220, width = 0.3, amp = 0.1, dur = 1;
    var env = Env.perc(0.05, dur * 0.8, curve: -3.0).kr(2);
    var saw = VarSaw.ar(
      freq: [freq, freq * 2, freq * 3, freq * 4],
      iphase: [ pi / 8, pi / 6, 2 * pi / 7, 2 * pi / 3 ],
      width: Line.kr(width, 0.7, dur * 0.4),
      mul: [amp, amp / 2, amp / 3, amp / 4]
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: Line.kr(1, 0.5, dur * 0.5) * freq * 8,
      rq: 0.2
    );
    Out.ar(outBus, Splay.ar(lpf));
  }).add;
)
(
  var freqs = [ 73.42, 65.41, 58.27, 69.3 ];
  Pbind(
    \instrument, \sawish,
    \freq, Pseq(freqs, 1),
    \amp, 0.4,
    \dur, 5
  ).play(TempoClock(72/60));
)
(
  SynthDef(\triEvil, {
    arg outBus = 0, freq1 = 622, freq2 = 830, dur = 1, amp = 0.1;
    var freqEnv = EnvGen.kr(
      Env.new([freq2, freq1, freq1], [dur * 0.0625, dur * 0.675], curve: [-2.5]),
    );
    var env = Env.perc(0.1, dur * 1.2, curve: -3).kr(2) * amp;
    var tri = LFPulse.ar(
      freq: [freqEnv, freqEnv * 0.99, freqEnv * 1.01],
      iphase: [pi / 6, pi / 3, pi / 2, 2 * pi / 3],
      width: SinOsc.kr(0.7).range(0.3, 0.7)
    ) * env;
    var lpf = RLPF.ar(
      in: tri,
      freq: Line.kr(freq1 * 8, freq1 * 16, dur * 0.75),
      rq: 0.1
    );
    var hpf = HPF.ar(
      in: lpf,
      freq: 110
    );
    var comb = BPF.ar(
      in: hpf,
      freq: Line.kr(freq1 * 2, freq1 * 8.75, dur * 0.8),
      rq: 0.2
    );
    var out = comb;
    Out.ar(outBus, Splay.ar(out));
  }).add;
)
Synth(\triEvil, [ freq1: 65.41, freq2: 58.27, dur: 4, amp: 0.5 ])
(
  var firstHarm = [
    233, 349.23, 261.63, 220, 293.66,
    220, 349.23, 233, 220, 329.63,
    329.63, 349.23, 233, 277.18, 311.13,
    220, 349.23, 233, 220, 329.63
  ];
  var durs = [ 0.5, 1, 0.5, 1, 2 ];
  var firstBouncy = firstHarm.collect({ arg f; f; });
  var secondHarm = [
    261.63, 233, 220, 370, 392,
    261.63, 233, 220, 392, 440,
    293.66, 261.63, 329.63, 293.66, 392,
    293.66, 261.63, 329.63, 392, 440,
    329.63, 349.23, 392, 293.66, 311.13,
    293.66, 261.63, 329.63, 392, 440
  ];
  var secondBouncy = secondHarm.collect({ arg f; f; });
  firstBouncy.addFirst(220).removeAt(firstBouncy.size() - 1);
  secondBouncy.addFirst(233).removeAt(secondBouncy.size() - 1);
  Pbind(
    \instrument, \triEvil,
    \freq1, Pseq(secondHarm, 1),
    \freq2, Pseq(secondBouncy, 1),
    \dur, Pseq(durs, inf),
    \amp, Prand([0.19, 0.18, 0.2, 0.21, 0.23], inf)
  ).play(TempoClock(72/60));
)
(
  SynthDef(\triPuro, {
    arg outBus = 0, freq1 = 622, freq2 = 830, dur = 1, length = 1, amp = 0.1;
    var freqEnv = EnvGen.kr(
      Env.new([freq2, freq1, freq1], [dur * 0.0625, dur * 0.675], curve: [-2.5]),
    );
    var env = Env.perc(0.1, length, curve: -3).kr(2) * amp;
    var tri = LFPulse.ar(
      freq: [freqEnv, freqEnv * 0.99, freqEnv * 1.01],
      iphase: [pi / 6, pi / 3, pi / 2, 2 * pi / 3],
      width: SinOsc.kr(0.7).range(0.3, 0.7)
    ) * env;
    var lpf = RLPF.ar(
      in: tri,
      freq: Line.kr(freq1 * 8, freq1 * 16, dur * 0.75),
      rq: 0.1
    );
    var comb = BPF.ar(
      in: lpf,
      freq: Line.kr(freq1 * 2, freq1 * 8.75, dur * 0.8),
      rq: 0.2
    );
    var out = comb;
    Out.ar(outBus, Splay.ar(out));
  }).add;
)
(
  var notasFirst = [
    65.41, 98, 65.41, 98, 65.41, 98, 65.41, 98, 65.41, 103.83, 92.50, 98
  ];
  var notasSecond = [
    82.41, 98, 82.41, 98, 82.41, 98, 82.41, 98, 82.41, 58.27, 92.50, 98
  ];
  var notasThird = [
    77.78, 98, 82.41, 98, 82.41, 98, 82.41, 98, 82.41, 98, 82.41, 98 
  ];
  var durs = [ 1, 4 ];
  var lengths = [ 1, 2 ];
  var bounciesFirst = notasFirst.collect({ arg f; f; });
  var bounciesSecond = notasSecond.collect({ arg f; f; });
  var bounciesThird = notasThird.collect({ arg f; f; });
  bounciesFirst.addFirst(58.27).removeAt(bounciesFirst.size() - 1);
  bounciesSecond.addFirst(73.42).removeAt(bounciesSecond.size() - 1);
  bounciesThird.addFirst(69.30).removeAt(bounciesSecond.size() - 1);
  Pbind(
    \instrument, \triPuro,
    \freq1, Pseq(notasThird, 1),
    \freq2, Pseq(bounciesThird, 1),
    \dur, Pseq(durs, inf),
    \length, Pseq(lengths, inf),
    \amp, Prand([0.19, 0.18, 0.2, 0.21, 0.23], inf)
  ).play(TempoClock(72/60));
)
(
  var ebphr = [
    51.91, 46.25, 58.27, 41.20,
    51.91, 46.25, 58.27,
    51.91, 41.20, 58.27, 46.25,
    51.91, 58.27, 46.25, 41.20,
    51.91, 46.25, 58.27,
    51.91, 46.25, 58.27, 41.20,
    51.91, 46.25, 58.27,
    51.91, 41.20, 58.27, 46.25,
    51.91, 58.27, 46.25, 41.20,
    51.91, 46.25, 49
  ];
  var dur = [
    3, 1, 1, 1,
    2, 2, 2,
    1, 2, 1, 2,
    2, 2, 1, 1,
    1, 3, 2
  ];
  var ebbounce = ebphr.collect({ arg f; f; });
  ebbounce.addFirst(46.25).removeAt(ebbounce.size() - 1);
  Pbind(
    \instrument, \triPuro,
    \freq1, Pseq(ebphr, 1),
    \freq2, Pseq(ebbounce, 1),
    \dur, Pseq(dur, 2),
    \length, Pseq(dur, 2),
    \amp, Prand([0.19, 0.18, 0.2, 0.21, 0.23], inf)
  ).play(TempoClock(72/60));
)

(
  SynthDef(\pluck, { | freq = 220, amp = 0.5, gate = 1, pan = 0, c3 = 20, c1 = 10 |
    var env = Env.new(
      levels: [0, 1, 0], 
      times: [SinOsc.kr(0.3).range(0.002, 0.01), 0.0005], 
      curve: [5, -8]
    );
    var inp = amp * LFClipNoise.ar(2000) * EnvGen.ar(env, gate);
    var pluck = DWGPlucked.ar(
      freq: freq,
      amp: amp, 
      gate: gate, 
      pos: SinOsc.kr(0.3).range(0.5, 0.9),
      c1: c1, 
      c3: c3,
      inp: inp
    );
    DetectSilence.ar(pluck, 0.001, doneAction: Done.freeSelf);
    Out.ar(0, Pan2.ar(pluck * 0.1, pan));
  }).add;
)
(
  var gm7ebm7 = [ 587.33, 698.46, 466.16 ];
  var cisdor7 = [ 554.37, 493.88, 659.26 ];
  Pbind(
    \instrument, \pluck,
    \freq, Prand(cisdor7, inf),
    \dur, Pseq([0.25, 0.25, 0.5, 0.5, 0.5, 0.5], inf),
    \amp, Pseq([
      0.3, 0.2, 0.2, 0.2, 0.2,
      0.3, 0.2, 0.2, 0.2, 0.2,
      0.3, 0.2, 0.2, 0.2, 0.2,
      0.3, 0.2, 0.2, 0.2, 0.2,
      0.3, 0.2, 0.2, 0.2
    ], inf),
    \c1, 100,
    \c3, 450,
    \pan, Prand([-0.8, -0.6, -0.3, 0, 0.3, 0.6, 0.8], inf)
  ).play(TempoClock(144/60));
)
(
  var freq = [622.25, 659.26, 659.26, 622.25, 554.37, 554.37, 554.37, 554.37];
  var dur1 = [0.5, 0.25, 0.25, 2.25, 0.25, 0.25, 0.25, 1];
  var dur2 = [0.5, 0.25, 0.25, 3, 0.25, 0.25, 0.25, 0.25];
  var dur = dur1 ++ dur1 ++ dur1 ++ dur1 ++ dur1 ++ dur1 ++
    dur2 ++  dur2 ++  dur2 ++  dur2 ++  dur2 ++  dur2; 
  Pbind(
    \instrument, \pluck,
    \freq, Pseq(freq, 12),
    \amp, Prand([0.5, 0.47, 0.53, 0.48, 0.51], inf),
    \dur, Pseq(dur, inf)
  ).play(TempoClock(72/60));
)
(
  SynthDef(\dustOfTime, { 
    arg density = 512, dur = 2, amp = 0.5;
    var dustEnv = XLine.kr(start: density, end: 12, dur: dur, doneAction: Done.freeSelf) ! 4;
    var dustAmp = amp * [1, 0.7, 0.6, 0.5, 0.7, 0.6, 1];
    var dust = Dust.ar(density: dustEnv, mul: dustAmp);
    Out.ar(
      0,
      Splay.ar(dust);
    );
  }).add;
)
(
  SynthDef(\funeralDrum, {
    arg outBus = 0, amp = 0.1, tension = 0.1;
    var env = Env.perc(0.01, 0.4166667, curve: -4).kr(2);
    var excitation = PinkNoise.ar(amp) * env;
    var hex = MembraneHexagon.ar(
      excitation: excitation,
      tension: tension,
      loss: 0.999
    );
    Out.ar(outBus, hex ! 2);
  }).add;
)
Synth(\dustOfTime, [ dur: 6, density: 2048, amp: 0.2 ]);
(
  Pbind(
    \instrument, \dustOfTime,
    \dur, 5,
    \amp, 0.2,
    \density, Prand([512, 1024, 256, 2096], inf)
  ).play(TempoClock(72/60));
)
Synth(\funeralDrum, [ amp: 0.1, tension: 0.004 ]);
(
  Pbind(
    \instrument, \funeralDrum,
    \amp, 0.1,
    \tension, 0.004,
    \dur, 10
  ).play(TempoClock(72/60));
)
(
  SynthDef(\triPud, {
    arg outBus = 0, freq1 = 622, freq2 = 830, dur = 1, amp = 0.1, length = 1;
    var freqEnv = EnvGen.kr(
      Env.new([freq2, freq1, freq1], [length * 0.0625, length * 0.675], curve: [-2.5]),
    );
    var env = Env.perc(0.05, length, curve: -3).kr(2) * amp;
    var tri = LFTri.ar(
      freq: [freqEnv, freqEnv * 0.99, freqEnv * 1.01],
      iphase: [pi / 6, pi / 3, pi / 2, 2 * pi / 3]
      // width: SinOsc.kr(0.7).range(0.3, 0.7)
    ) * env;
    var lpf = RLPF.ar(
      in: tri,
      freq: Line.kr(freq1 * 8, freq1 * 16, dur * 0.75),
      rq: 0.1
    );
    var comb = BPF.ar(
      in: lpf,
      freq: Line.kr(freq1 * 2, freq1 * 8.75, dur * 0.8),
      rq: 0.2
    );
    var out = comb;
    Out.ar(outBus, Splay.ar(out));
  }).add;
)
(
  var freqs = [
    349.23, 392, 440,
    349.23, 392, 440,
    349.23, 466.16, 622.25,
    392, 523.25, 698.46,
    329.63, 440, 587.33,
    370, 493.88, 659.26
  ] * 0.5;
  var durs = [ 0.5, 0.25, 4.25 ];
  var lengths = [ 0.5, 0.25, 2.75 ];
  var bouncies = freqs.collect({ arg f; f; });
  bouncies.addFirst(311.13).removeAt(bouncies.size() - 1);
  Pbind(
    \instrument, \triPud,
    \freq1, Pseq(freqs, 1),
    \freq2, Pseq(bouncies, 1),
    \dur, Pseq(durs, inf),
    \amp, Prand([0.2, 0.19, 0.18, 0.21, 0.22], inf)
  ).play(TempoClock(72/60));
)
