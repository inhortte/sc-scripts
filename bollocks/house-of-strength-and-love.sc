(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\sawMeInThirds, {
    arg outBus = 0, sinFreq = 1, syncPos = 54, syncNeg = 54, fundamental = 220, slave = 440, amp = 0.1, dur = 1;
    var env = Env.perc(0.01 * dur, dur * 0.48, curve: -4).kr(2);
    /*
    var env = EnvGen.ar(
      Env.new([0, amp, 0.64 * amp, 0], [0.05 * dur, dur * 0.72, dur * 0.16], curve: [-5, 2, 1.2]),
      doneAction: Done.freeSelf
    );
    */
    var saw = SyncSaw.ar(
      syncFreq: fundamental,
      sawFreq: SinOsc.kr(sinFreq, pi / 2).range(slave - syncNeg, slave + syncPos),
    ) * env;
    var bpf = BPF.ar(
      in: saw,
      freq: SinOsc.kr(sinFreq / 8, 0).range(fundamental * 4, slave * 5),
      rq: 0.8
    );
    var notch = LPF.ar(
      in: bpf,
      freq: SinOsc.kr(sinFreq / 24, pi / 2).range(fundamental * 4, slave * 6)
      // rq: 1.2
    );
    Out.ar(
      outBus,
      notch ! 2
    );
  }).add;
  SynthDef(\sawMeGently, {
    arg outBus = 0, sinFreq = 1, fundamental = 220, amp = 0.1, dur = 1;
    var env = Env.perc(0.01 * dur, dur * 0.32, curve: -4).kr(2);
    var saw = VarSaw.ar(
      freq: [fundamental, fundamental * 1.01, fundamental * 0.99],
      iphase: SinOsc.kr(sinFreq / 2, 0).range(2 * pi / 6, 4 * pi / 6),
      width: SinOsc.kr(sinFreq, pi / 2).range(0.1, 0.5)
    ) * env * amp;
    Out.ar(outBus, Splay.ar(saw)); 
  }).add;
)
Synth(\sawMeInThirds, [ sinFreq: 4, syncPos: 18, syncNeg: 44, fundamental: 247, slave: 311, amp: 0.3, dur: 1.5 ]);
Synth(\sawMeGently, [ sinFreq: 12, fundamental: 220, amp: 0.3, dur: 5 ]);
(
  SynthDef(\notch, {
    arg inBus = 7, outBus = 0, sinFreq = 1, fundamental = 220;
    var notch = Notch.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(sinFreq / 12, pi / 2).range(fundamental * 6, fundamental * 1.5 * 7),
      rq: 1.2
    );
    Out.ar(outBus, notch);
  }).add;
)
[1,3,4].wrapExtend(16)
10 % 4 == 2
(
  var clock = TempoClock(100/60);
  var ebSeq = [ 311, 494, 659 ].wrapExtend(16).scramble.collect({
    arg item, idx;
    case
      { idx % 8 == 6 } { [ item, item, item ] }
      { idx % 8 == 2 } { [ item, item ] }
      { true } { item };
  }).flat;
  var bSeq = [ 277, 494, 659 ].wrapExtend(16).scramble.collect({
    arg item, idx;
    case
      { idx % 8 == 6 } { [ item, item, item ] }
      { idx % 8 == 2 } { [ item, item ] }
      { true } { item };
  }).flat;
  var bbSeq = [ 247, 466, 659 ].wrapExtend(16).scramble.collect({
    arg item, idx;
    case
      { idx % 8 == 6 } { [ item, item, item ] }
      { idx % 8 == 2 } { [ item, item ] }
      { true } { item };
  }).flat;
  var fisSeq = [ 277, 330, 370 ].wrapExtend(9).scramble.collect({
    arg item, idx;
    case
      { idx % 8 == 6 } { [ item, item, item ] }
      { idx % 8 == 2 } { [ item, item ] }
      { true } { item };
  }).flat;
  var amps = [ 0.4, 0.5, 0.6 ];
  var durs = [ 
    0.5, 0.5, 0.25, 0.25, 0.5,
    0.5, 0.5, 0.166666665, 0.166666665, 0.166666665, 0.5
  ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \sawMeGently,
    \fundamental, Pseq((ebSeq ++ bSeq ++ bbSeq ++ fisSeq) / 2, 1),
    \amps, Prand(amps, inf),
    \dur, Pseq(durs, inf),
    \outBus, pBus,
    \group, g,
    \addAction, 0,
    \sinFreq, 12
  ).play(clock);
  Synth.tail(g, \notch, [ inBus: pBus, outBus: 0, sinFreq: 1.2, fundamental: 277 ]);
)
(
  var clock = TempoClock(100/60);
  var fundamentalsE = [ 
    247, 311, 247, 330, 294, 247, 220, 277, 330, 311, 10,
    247, 311, 247, 330, 294, 233, 262, 294, 330, 10,
    247, 311, 247, 330, 294, 247, 220, 277, 330, 311, 10,
    247, 311, 247, 330, 294, 233, 262, 294, 311, 10
  ];
  var fundamentalsF = [
    262, 330, 262, 349, 311, 262, 233, 294, 349, 330, 10,
    262, 330, 262, 349, 311, 247, 277, 311, 349, 10, 
    262, 330, 262, 349, 311, 262, 233, 294, 349, 330, 10,
    262, 330, 262, 349, 311, 247, 277, 311, 330, 10
  ];
  var dursE = [ 1.5, 1.5, 1.5, 1.5, 2, 1.5, 1.5, 1.5, 1.5, 2, 4, 1.5, 1.5, 1.5, 1.5, 2, 1.5, 1.5, 1.5, 3.5, 8 ];
  var dursF = [ 1.5, 1.5, 1.5, 1.5, 2, 1.5, 1.5, 1.5, 1.5, 2, 8, 1.5, 1.5, 1.5, 1.5, 2, 1.5, 1.5, 1.5, 3.5, 12 ];
  var amps = [ 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0 ];
  var slavesE =  [
    311, 370, 330, 392, 370, 311, 294, 330, 440, 392, 10,
    311, 370, 330, 392, 370, 294, 330, 392, 440, 10,
    311, 370, 330, 392, 370, 311, 294, 330, 440, 392, 10,
    311, 370, 330, 392, 370, 294, 330, 392, 415, 10
  ];
  var slavesF = [
    330, 392, 349, 415, 392,  330, 311, 349, 466, 415, 10,
    330, 392, 349, 415, 392, 311, 349, 415, 466, 10,
    330, 392, 349, 415, 392, 330, 311, 349, 466, 415, 10,
    330, 392, 349, 415, 392, 311, 349, 415, 440, 10 
  ];
  var syncPosE = [
    18, 45, 110, 74, 45, 18, 121, 40, 182, 48, 10,
    18, 45, 110, 74, 45, 17, 21, 131, 147, 10,
    18, 45, 110, 74, 45, 18, 121, 40, 182, 48, 10,
    18, 45, 110, 74, 45, 17, 21, 131, 139, 10
  ];
  var syncPosF = [
    19, 48, 117, 79, 48, 19, 129, 43, 193, 51, 10,
    19, 48, 117, 79, 48, 19, 21, 139, 156, 10,
    19, 48, 117, 79, 48, 19, 129, 43, 193, 51, 10,
    19, 48, 117, 79, 48, 19, 21, 139, 147, 10
  ];
  var syncNegE = [
    44, 40, 83, 62, 40, 44, 74, 19, 110, 43, 10,
    44, 40, 83, 62, 40, 32, 36, 98, 110, 10,
    44, 40, 83, 62, 40, 44, 74, 19, 110, 43, 10,
    44, 40, 83, 62, 40, 32, 36, 98, 104, 10
  ];
  var syncNegF = [
    36, 43, 87, 66, 43, 19, 78, 19, 117, 45, 10,
    36, 43, 87, 66, 43, 34, 38, 104, 117, 10,
    36, 43, 87, 66, 43, 19, 78, 19, 117, 45, 10,
    36, 43, 87, 66, 43, 34, 38, 104, 110, 10
  ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \sawMeInThirds,
    \outBus, pBus,
    \group, g,
    \addAction, 0,
    \sinFreq, (100 / 60) * 4,
    \dur, Pseq(dursF, inf),
    \amp, Pseq(amps, inf),
    \fundamental, Pseq(fundamentalsF, 1),
    \slave, Pseq(slavesF, 1),
    \syncPos, Pseq(syncPosF, 1),
    \syncNeg, Pseq(syncNegF, 1)
  ).play(clock);
  Synth.tail(g, \notch, [ inBus: pBus, outBus: 0, sinFreq: (100 / 60) / 12, fundamental: 330 ]);
)
[0.5, 1] *.t [10, 20] * { LFNoise1.kr(10, 0.003, 1)}!2;
Vowel([\u, \e], [\bass, \soprano]);
(
  SynthDef(\octaPad, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    /*
    var env = EnvGen.ar(
      Env.new([0, 1, 0.8, 0], [dur * 0.375, 0.625 * dur, dur * 0.25], curve: [-0.5, 3, 2]),
      levelScale: amp,
      doneAction: Done.freeSelf
    );
    */
    var env = Env.perc(0.02, dur * 0.8, curve: -2).kr(2);
    var formants = Formants.ar(
      baseFreq: ([0.5, 1] *.t [freq, freq * 2] * {LFNoise1.kr(10, 0.003, 1)}!2).flat,
      vowel: Vowel([\u, \e], [\bass, \soprano]),
      freqMods: LFNoise2.ar(2 * [0.2, 0.3].scramble, 0.1),
      ampMods: env * [ amp, amp * 0.4 ]
    );
    var reverb = FreeVerb.ar(
      in: Splay.ar(formants.flat.scramble),
      mix: 0.4,
      room: 0.3,
      damp: 0.8
    );
    Out.ar(outBus, formants);
  }).add;
)
Synth(\octaPad, [ freq: 220, dur: 2, amp: 0.2 ]);
(
  SynthDef(\parabolic, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var pEnv = Env.perc(0.04, dur * 0.8, curve: -3).kr(2);
    var nEnv = Env.perc(dur * 0.4, 0, curve: -4).kr(0);
    var parabolic = LFSaw.ar(
      freq: [freq, freq / 2 * LFNoise1.kr(5, 0.02, 1)],
      iphase: SinOsc.kr(4, pi / 2).range(pi / 4, 2pi / 4),
      // width: SinOsc.kr(2, pi / 2).range(0.3, 0.7),
      mul: [amp, amp * 0.6] * pEnv
    );
    var noise = BrownNoise.ar(amp * 0.1) * nEnv ! 2;
    var lpf = MoogFF.ar(
      in: parabolic,
      gain: 3,
      freq: SinOsc.kr(0.5, pi / 2).range(freq * 0.5, freq * 1.5)
    );
    var mix = Mix([ noise, Splay.ar(lpf) ]);
    var bpf = BMoog.ar(
      in: mix,
      q: 0.6,
      mode: 2.5,
      freq: SinOsc.kr(0.3, 0).range(freq , freq * 3)
    );
    Out.ar(outBus, bpf);
  }).add;
)
(
  var clock = TempoClock(100/60);
  var melody = [
    1245, 1245, 1109, 1245, 1109, // Eb quartal
    1245, 1175, 988, 1175, 988, // B quartal
    1175, 1109, 932, 1109, 932, // Bb dim7
    1109, 988, 880, 988, 880 // F# quartal
  ];
  var durs = [ 0.5, 1, 0.5, 0.5, 1.5 + 4 ];
  var amps = [ 0.8, 1, 1, 0.8, 0.9 ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \parabolic,
    \dur, Pseq(durs, inf),
    \amp, Pseq(amps, inf),
    \freq, Pseq(melody, inf)
  ).play(clock);
)
// Tests
(
  plot { [
    SyncSaw.ar(800, 1200),
    Impulse.ar(800)
  ] }
)
Env.new([0, 1, 0.64, 0], [0.001, 1 * 0.88, 1 * 0.16], curve: [-5, 2, 1.2]).plot;
