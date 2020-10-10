(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\sawMyFaceOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var halfUp = freq * 2 - freq * ( 1 / 12 ) + freq;
    var freqEnv = EnvGen.ar(
      Env.new([freq, halfUp, freq, halfUp], [dur * 0.25, dur * 0.25, dur * 0.25], curve: [0, -2, -1])
    );
    var env = Env.perc(0.01, dur, curve: -7).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freq,
      iphase: 1.2
    ) * env;
    var bpf = BPF.ar(
      in: saw,
      // freq: freq * 4,
      freq: SinOsc.kr(60 / 72 * 2).range(freq * 3, freq * 5),
      rq: 0.5
    );
    var lpf = RLPF.ar(
      in: bpf,
      freq: SinOsc.kr(60 / 72 / 2).range(500, 900),
      rq: 0.2
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, lpf ! 2);
  }).add;
)
(
  SynthDef(\sawMyBulk, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var halfUp = freq * 2 - freq * ( 1 / 12 ) + freq;
    var env = Env.perc(2, dur, curve: -7).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freq,
      iphase: 0,
    ) * env * 0.5;
    var saw2 = LFSaw.ar(
      freq: freq * 1.01,
      iphase: 0.3
    ) * env * 0.5;
    var mix = Mix.new([saw, saw2] * 0.4);
    var bpf = BPF.ar(
      in: mix,
      // freq: freq * 4,
      freq: SinOsc.kr(60 / 72 * 3)).range(freq * 5, freq * 9),
      rq: 1.2
    );
    var lpf = RLPF.ar(
      in: bpf,
      freq: SinOsc.kr(60 / 72 / 2).range(800, 1200),
      rq: 1
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, allPass ! 2);
  }).add;
)
(
  j = Group.new;
  k = Group.new;
  l = Group.new;
)
(
  var ebMajor = [ 3, 5, 7, 8, 10, 12, 14 ];
  var durs = Array.fill(48, { 16 }).collect({
    arg item, idx;
    if((idx % 6 == 0) || ((idx - 1) % 6 == 0), { 12 }, { item });
  });
  var dappled = Array.fill(48, { 12 }).collect({
    arg item, idx;
    if((idx % 8 == 0) || ((idx - 1) % 8 == 0), { 8 }, { item });
  });
  var thurk = Pbind(
    \instrument, \sawMyFaceOff,
    \group, j,
    \dur, Pseq(durs, inf),
    \octave, 2,
    \scale, ebMajor,
    \degree, Pseq([ Prand(#[1, 2, 8, 5]) ], inf),
    \amp, 0.1
  );
  var smafco = Pbind(
    \instrument, \sawMyFaceOff,
    \group, j,
    \dur, Pseq(dappled, inf),
    \octave, 3,
    \scale, ebMajor,
    \depree, Pseq([ Prand(#[0, 1, 7, 8, 4, 11, 15]) ], inf),
    \amp, 0.1
  );
  Ppar([ thurk, smafco ]).play(TempoClock(72/60));
)
(
  var ebMajor = [ 3, 5, 7, 8, 10, 12, 14 ];
  var durs = Array.fill(48, { Prand(#[12, 10], inf) }).collect({
    arg item, idx;
    if((idx % 7 == 0) || ((idx - 1) % 7 == 0), { 6 }, { item });
  });
  var dappled = Array.fill(48, { Prand(#[8, 7], inf) }).collect({
    arg item, idx;
    if((idx % 5 == 0) || ((idx - 1) % 5 == 0), { 4 }, { item });
  });
  var thurk = Pbind(
    \instrument, \sawMyBulk,
    \group, k,
    \dur, Pseq(durs, inf),
    \octave, 3,
    \scale, ebMajor,
    \degree, Pseq([ Prand(#[0, 1, 7, 4, 3]) ], inf),
    \amp, 0.1
  );
  var smafco = Pbind(
    \instrument, \sawMyBulk,
    \group, k,
    \dur, Pseq(dappled, inf),
    \octave, 4,
    \scale, ebMajor,
    \depree, Pseq([ Prand(#[1, 3, 8, 9, 5, 12, 16]) ], inf),
    \amp, 0.1
  );
  Ppar([ thurk, smafco ]).play(TempoClock(72/60));
)
(
  var ebMajor = [ 3, 5, 7, 8, 10, 12, 14 ];
  var durs = Array.fill(48, { Prand(#[16, 15, 14, 7], inf) }).collect({
    arg item, idx;
    if((idx % 6 == 0) || ((idx - 1) % 6 == 0), { 12 }, { item });
  });
  var dappled = Array.fill(48, { Prand(#[12, 11, 10, 5], inf) }).collect({
    arg item, idx;
    if((idx % 8 == 0) || ((idx - 1) % 8 == 0), { 8 }, { item });
  });
  var thurk = Pbind(
    \instrument, \sawMyFaceOff,
    \group, l,
    \dur, Pseq(durs, inf),
    \octave, 2,
    \scale, ebMajor,
    \degree, Pseq([ Prand(#[1, 2, 8, 5]) ], inf),
    \amp, 0.1
  );
  var smafco = Pbind(
    \instrument, \sawMyFaceOff,
    \group, l,
    \dur, Pseq(dappled, inf),
    \octave, 3,
    \scale, ebMajor,
    \depree, Pseq([ Prand(#[0, 1, 7, 8, 4, 11, 15]) ], inf),
    \amp, 0.1
  );
  Ppar([ thurk, smafco ]).play(TempoClock(72/60));
)
j.set(\amp, 0.1);
l.set(\amp, 0.0);
k.set(\amp, 0.0);
k.deepFree;
