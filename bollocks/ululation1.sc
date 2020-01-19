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
