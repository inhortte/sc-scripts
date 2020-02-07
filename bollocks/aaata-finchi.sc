(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\highTri, {
    arg outBus = 0, dur = 1, amp = 0.1, freq = 220;
    var triEnv = Env.perc(0.05, dur * 0.4, curve: -1).kr(2);
    var sawEnv = Env.perc(0.07, dur * 0.4, curve: -3).kr(0);
    var tri1 = LFTri.ar(
      freq: freq,
      iphase: [ 1.5, 3.2 ]
    ) * triEnv * amp;
    var bpf = BPF.ar(
      in: tri1,
      freq: SinOsc.kr(0.3).range(freq * 2, freq * 5),
      rq: 0.3
    );
    var saw1 = LFSaw.ar(
      freq: freq,
      iphase: [ 0.5, 1.3 ]
    ) * sawEnv * amp * 0.3;
    var bpf2 = BPF.ar(
      in: saw1,
      freq: SinOsc.kr(0.1, pi / 2).range(freq * 1, freq * 3),
      rq: 0.1
    );
    var mix = Mix.new([ Splay.ar(bpf), Splay.ar(bpf2) ]);
    var resEnv = EnvGen.ar(
      Env.new([SinOsc.kr(0.2).range(freq * 0.2, freq * 4) , freq * 4], [ dur * 0.4 ], [ 2 ])
    );
    var rlpf = RLPF.ar(
      in: mix,
      freq: resEnv,
      rq: 0.3
    );
    var lpf = LPF.ar(
      in: rlpf,
      freq: SinOsc.kr(0.4).range(freq * 0.7, freq * 2)
    );
    Out.ar(outBus, Splay.ar(lpf));
  }).add;
)
Synth(\highTri, [ freq: 440, dur: 1, amp: 0.3 ]);
(
  var durs = Array.fill(48, { 0.5 }).collect({
    arg item, idx;
    if((idx % 6 == 0) || ((idx - 1) % 6 == 0), { 0.25 }, { item });
  });
  durs.postln;
)
(
  var aMajor = [ 9, 11, 13, 14, 16, 18, 20 ];
  var besHarmMaj = [ ];
  var durs = Array.fill(48, { 0.5 }).collect({
    arg item, idx;
    if((idx % 6 == 0) || ((idx - 1) % 6 == 0), { 0.25 }, { item });
  });
  var thurk = Pbind(
    \instrument, \highTri,
    \dur, 0.5,
    \octave, 4,
    \scale, aMajor,
    \degree, Pseq([ Pseq([ 2, 3 ]), Prand(#[5, 6]) ], inf),
    \amp, 0.3
  );
  Ppar([ thurk ]).play(TempoClock(90/60));
)

(
  SynthDef(\pleasantPulse, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1, attack = 0.03;
    var env = Env.perc(attack, dur * 0.8, curve: -2).kr(2);
    var pulse = LFPulse.ar(
      freq: [ freq, freq * 0.995, freq * 1.005 ],
      iphase: [ 0.2, 0.6, 0.95 ],
      width: SinOsc.kr(0.2).range(-0.3, 0.3)
    ) * env * amp;
    Out.ar(outBus, Splay.ar(pulse));
  }).add;
)
Synth(\pleasantPulse, [ freq: 220, dur: 2, amp: 0.3 ]);
(
  SynthDef(\puckFilter, {
    arg inBus = 7, outBus = 0, low = 277, high = 830, bpm = 84, div = 3;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(1/(60/bpm/div), 0).range(low, high),
      rq: 0.2
    );
    /*
    var bpf = GlitchBPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(1/(60/bpm/div), 0).range(low * 3, high * 3.2),
      rq: 0.1,
      mul: 3
    );
    */
    // var mix = Mix([ lpf * 0.5, bpf * 0.5 ]);
    var mix = lpf;
    Out.ar(outBus, mix);
  }).add;
)

// Dm9 ost
(
  var besHarmMaj = [ -2, 0, 2, 3, 5, 6, 9 ];
  var pBus = Bus.audio(s, 2);
  var ost, ost2;
  g = Group.basicNew(s, 1);
  ost = Pbind(
    \instrument, \pleasantPulse,
    \outBus, pBus,
    \group, g,
    \octave, 3,
    \scale, besHarmMaj,
    \degree, Pseq([ 8, 5, 6, 5, 3 ], inf),
    \dur, Pseq([ 4, 0.5, 3.5, 0.5, 5.5 ], inf),
    \amp, 0.1
  );
  ost2 = Pbind(
    \instrument, \pleasantPulse,
    \outBus, pBus,
    \group, g,
    \octave, 3,
    \scale, besHarmMaj,
    \degree, Pseq([ 3, 7 ], inf),
    \dur, Pseq([ 8, 6 ], inf)
  );
  Ppar([ ost, ost2 ]).play(TempoClock(108/60));
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0, low: 660, high: 1760, bpm: 108, div: 0.14 ]);
)

// Gmsus9 ost / F#msus9 (with the ctranspose -1)
(
  var gAeolean = [ -5, -3, -2, 0, 2, 3, 5 ];
  var ost, ost2;
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  ost = Pbind(
    \instrument, \pleasantPulse,
    \outBus, pBus,
    \ctranspose, -1,
    \group, g,
    \octave, 4,
    \scale, gAeolean,
    \degree, Pseq([ 2, 0, 1, -1, -2 ], inf),
    \dur, Pseq([ 4, 0.5, 3.5, 0.5, 5.5 ], inf),
    \amp, 0.1
  );
  ost2 = Pbind(
    \instrument, \pleasantPulse,
    \outBus, pBus,
    \ctranspose, -1,
    \group, g,
    \octave, 4,
    \scale, gAeolean,
    \degree, Pseq([ -3, 1 ], inf),
    \dur, Pseq([ 8, 6 ], inf)
  );
  Ppar([ ost, ost2 ]).play(TempoClock(108/60));
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0, low: 660, high: 1760, bpm: 108, div: 0.14 ]);
)

// Flyd dom7
(
  var cMelMin = [ 0, 2, 3, 5, 7, 9, 11 ];
  var ost, ost2;
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  ost = Pbind(
    \instrument, \pleasantPulse,
    \outBus, pBus,
    \group, g,
    \octave, 3,
    \scale, cMelMin,
    \degree, Pseq([ 7, 3, 4, 5, 2 ], inf),
    \dur, Pseq([ 4, 0.5, 3.5, 0.5, 5.5 ], inf),
    \amp, 0.1
  );
  ost2 = Pbind(
    \instrument, \pleasantPulse,
    \outBus, pBus,
    \group, g,
    \octave, 3,
    \scale, cMelMin,
    \degree, Pseq([ 2, 6 ], inf),
    \dur, Pseq([ 8, 6 ], inf)
  );
  Ppar([ ost, ost2 ]).play(TempoClock(108/60));
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0, low: 660, high: 1760, bpm: 108, div: 0.14 ]);
)

// transition melody
(
  var cMelMin = [ 0, 2, 3, 5, 7, 9, 11 ];
  var bMelMin = [ -1, 1, 2, 4, 6, 8, 10 ];
  var besHarmMaj = [ -2, 0, 2, 3, 5, 6, 9 ];
  var gAeolean = [ -5, -3, -2, 0, 2, 3, 5 ];
  var fisChromatic = [ -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5 ];
  var mel1, mel2, mel3, mel4, mel5;
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  mel1 = Pbind(
    \instrument, \pleasantPulse, \outBus, pBus, \group, g, \octave, 3,
    \scale, besHarmMaj,
    \degree, Pseq([ \, 4, 8, 9, 0, 4, 5, 2, 3, 7, 8 ], 1),
    \dur, Pseq([ 1, 1, 2.5, 1, 0.5, 0.5, 0.5, 2.5, 0.5, 0.5, 5.5 ], 1),
    \amp, 0.1
  );
  mel2 = Pbind(
    \instrument, \pleasantPulse, \outBus, pBus, \group, g, \octave, 3,
    \scale, gAeolean,
    \degree, Pseq([ \, 6, 10, 11, 9, 6, 7 ], 1),
    \dur, Pseq([ 1, 1, 3.5, 1, 0.5, 0.5, 0.5 ], 1),
    \amp, 0.1
  );
  mel3 = Pbind(
    \instrument, \pleasantPulse, \outBus, pBus, \group, g, \octave, 3,
    \scale, fisChromatic,
    \degree, Pseq([ 7, 8, 15, 17, 9, 11, 13, 12 ], 1),
    \dur, Pseq([ 2.5, 0.5, 0.5, 1, 0.5, 0.5, 1, 9.5 ], 1),
    \amp, 0.1
  );
  mel4 = Pbind(
    \instrument, \pleasantPulse, \outBus, pBus, \group, g, \octave, 3,
    \scale, cMelMin,
    \degree, Pseq([ 3, 7, 8, 6, 3, 4, 1, 2 ], 1),
    \dur, Pseq([ 1, 2.5, 1, 0.5, 0.5, 0.5, 2.5, 0.5 ], 1),
    \amp, 0.1
  );
  mel5 = Pbind(
    \instrument, \pleasantPulse, \outBus, pBus, \group, g, \octave, 3,
    \scale, bMelMin,
    \degree, Pseq([ 5, 6 ], 1),
    \dur, Pseq([ 0.5, 5.5 ], 1),
    \amp, 0.1
  );
  Ptpar([
    0.0, mel1,
    24.0, mel2,
    32.0, mel3,
    49.0, mel4,
    57.5, mel5
  ]).play(TempoClock(108/60));
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0, low: 660, high: 1760, bpm: 108, div: 0.14 ]);
)
(
  var cChromatic = [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
  var mel1;
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  mel1 = Pbind(
    \instrument, \pleasantPulse, \outBus, pBus, \group, g, \octave, 3,
    \scale, cChromatic,
    \attack, 0.001,
    \degree, Pseq([ 
      2, 9, 10, 12, 6, 5, 2, 3, -2, 0, -2, 0, 6, 7, 9, \ , // Dm9
      \, \, \,
      2, 9, 10, 12, 6, 5, 2, 3, -2,
      3, 5, 3, 9, 10, 12, 9, 10, 12, 7, 5, 2, 3, -2, 0, -2, \, // Gmsus9
      \,
      3, -3, 1, 4, 3, 4, -2, 2, 5, 4,
      \,
      -1, 4, 8, 6, 8, 6, 1, 2, -3, -1, -3, // F#msus9
      \, 11, 10, 4, 6, 10, 7, 3, 4, 6, 7, 8, 4, 3, 
      4, 8, 13, 11, 9, 6,
      5, 12, 11, 14, 5, 12, 11, 14, \, 4, 11, 9, 12, \, // C mel min / E harm min
      11, 12, 5, 7, 2, 3, 12, 13, 14, 0, 9, 11, 12,
      14, 12, 7, 9, 3, 5, 9, 11, 12, 4, 5, 7, 6, 2,
      2, 3, 7, 12, 11, 5, 5, 7, 11, 16, 15, 9 // F lyd dom
    ], 1),
    \dur, Pseq([ 
      1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 1, 1.5, 0.5, 0.5, 0.5, 2, 2,
      3, 2.5, 2.5,
      1, 1, 1, 1, 0.5, 0.5, 1, 1, 1,
      1, 1, 1, 0.5, 0.5, 1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 1, 2, 1,
      12,
      1.5, 0.5, 0.5, 0.5, 1, 1.5, 0.5, 0.5, 0.5, 2,
      7,
      1, 1, 1, 1.5, 0.5, 1, 1, 1.5, 0.5, 1, 1,
      1.5, 0.25, 0.25, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 1, 1, 1, 1, 2.5,
      0.5, 0.5, 0.5, 1.5, 0.5, 2,
      1, 1, 1, 1.5, 0.5, 0.5, 0.5, 1.5, 1, 0.5, 0.5, 0.5, 1.5, 0.5,
      1, 1, 1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 0.5, 0.5, 1, 0.5, 0.5, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 
    ], 1),
    \amp, 0.1
  );
  Ptpar([
    0.0, mel1
  ]).play(TempoClock(108/60));
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0, low: 660, high: 1760, bpm: 108, div: 0.14 ]);
)

{ Crackle.ar(MouseX.kr(1,2), 0.5) }.scope(1);
{ Dust.ar(50) }.scope(1, zoom: 4);
(
  // allocate tables 80 to 87
  16.do {|i| s.sendMsg(\b_alloc, 80+i, 1024); };
  /*
  8.do({|i|
      // generate array of harmonic amplitudes
      a = {1.0.rand2.cubed }.dup((i+1)*1.5);
      // fill table
      s.listSendMsg([\b_gen, 80+i, \sine1, 7] ++ a);
  });    
  */
  /*
  8.do({|i|
    var n, a;
    // generate array of harmonic amplitudes
    n = (i+1)**1.5; // num harmonics for each table: [1,4,9,16,25,36,49,64]
    a = {|j| ((n-j)/n).squared }.dup(n);
    // fill table
    s.listSendMsg([\b_gen, 80+i, \sine1, 7] ++ a);
  });
  */
  8.do({ |i|
    var tableNumber = i * 2;
    var n, a, b;
    a = { 1.0.rand2.cubed }.dup((i + 1) * 1.8);
    s.listSendMsg([ \b_gen, 80 + tableNumber, \sine1, 7 ] ++ a);
    n = (i + 1)**1.8;
    b = {|j| ((n - j) / n).squared }.dup(n);
    s.listSendMsg([\b_gen, 80 + tableNumber + 1, \sine1, 7 ] ++ b);
  });
  SynthDef(\sinTable, {
    arg outBus = 0, freq = 110, dur = 1, amp = 0.1;
    var vosc, rlpf;
    // var env = Env.perc(0.2, dur * 1.3, curve: 1).kr(2);
    var dustEnv = EnvGen.kr(
      // Env.perc(0.001, SinOsc.kr(0.3).range(0.01, 0.04), curve: -3),
      Env.perc(0.001, 0.04, curve: -3),
      gate: Dust.ar(SinOsc.kr(0.3).range(512, 1024))
    );
    var crackleEnv = EnvGen.kr(
      Env.perc(0.1, 0.2, curve: -3),
      gate: Crackle.kr(1.4)
    );
    vosc = VOsc3.ar(
      [ SinOsc.kr(0.1).range(80, 84), SinOsc.kr(0.15).range(84, 89), SinOsc.kr(0.05).range(82, 86) ],
      // [ MouseX.kr(80,85), MouseX.kr(81,86), MouseX.kr(82,87) ], 
      freq, 
      0.9875 * freq,
      1.0125 * freq,
      [ amp, amp * 0.8, amp * 0.5 ] * 0.8
    ); // * dustEnv;
    rlpf = RLPF.ar(
      in: vosc,
      freq: SinOsc.kr(0.1).range(freq * 5, freq * 16),
      rq: [ 0.1, 0.3, 0.6 ]
    );
    Out.ar(0, Splay.ar(rlpf));
  }).add;
)
Synth(\sinTable, [ freq: 110 ]);
(
  var bastard = Synth(\sinTable, [ freq: 146.83 ]); // d
  var freqs = [
    146.83, 130.81, 146.83, 130.81, 146.83, 130.81, // d c d c d c
    174.61, 146.83, 174.61, 146.83, 174.61, // f d f d f
    164.81, 138.59, 164.81, 138.59, 164.81, // e cis e
    146.83, 196, 146.83, 174.61, 196, 185 // d g d f fis
  ] * 4;
  var durs = [ 
    8, 8, 8, 8, 8, 8,
    8, 8, 4, 8, 8,
    8, 8, 4, 8, 8,
    8, 8, 8, 8, 8, 8
  ] * (60 / 108);
  Routine({
    freqs.size.do({ |idx|
      freqs.at(idx).postln;
      bastard.set(\freq, freqs.at(idx));
      durs.at(idx).wait;
    });
  }).play;
)
(
  var bastard = Synth(\sinTable, [ freq: 110 ]); // a
  var freqs = [
    110, 138.59, 123.47, 82.41, // a cis b e
    110, 138.59, 123.47, 82.41,
    110, 138.59, 123.47, 82.41,
    103.83, 98, 138.59, 87.31, // gis g cis f
    103.83, 98, 138.59, 87.31, 
    110, 130.81, 116.54, 87.31, // a c ais f
    110, 130.81, 116.54, 87.31
  ];
  var durs = Array.fill(7, { [ 22, 4, 4, 4 ] }).flatten * (60 / 108);
  Routine({
    freqs.size.do({ |idx|
      freqs.at(idx).postln;
      bastard.set(\freq, freqs.at(idx));
      durs.at(idx).wait;
    });
  }).play;
)
Array.fill(7, { [ 22, 4, 4, 4 ] }).flatten.postln
