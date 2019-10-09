(
  s.options.numBuffers = 8192;
  s.boot;
)
s.plotTree

(
  SynthDef(\duoPad, {
    arg outBus = 0, freq1 = 622, freq2 = 830, dur = 1, amp = 0.1;
    var env = EnvGen.ar(
      Env.new([0, 1, 0.6, 0], [dur * 0.178, 0.536 * dur, dur * 0.178], curve: [-0.5, 3, 2]),
      levelScale: amp,
      doneAction: Done.freeSelf
    );
    var formants = Formants.ar(
      baseFreq: ([1, 2, 3 ,4] *.t [freq1, freq2] * {LFNoise1.kr(10, 0.003, 1)}!4).flat,
      vowel: Vowel([\u, \e], [\tenor, \soprano]),
      freqMods: LFNoise2.ar(4 * [0.1, 0.2, 0.3, 0.4, 0.5].scramble, 0.1),
      ampMods: env
    );
    var reverb = FreeVerb.ar(
      in: Splay.ar(formants.flat.scramble),
      mix: 0.2,
      room: 0.3,
      damp: 0.2
    );
    Out.ar(outBus, reverb);
  }).add;
)
(
  SynthDef(\brawny, {
    arg inBus = 7, outBus = 0, sinFreq = 1, fundamental = 220;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(sinFreq, pi / 2).range(fundamental * 4, fundamental * 1.5 * 7),
      rq: 0.1
    );
    Out.ar(outBus, lpf);
  }).add;
)
Synth(\duoPad, [ freq1: 349, freq: 622, dur: 3, amp: 0.2 ]);

(
  var clock = TempoClock(84/60);
  var freq1 = [ 
    247, 220, 247, 220, 208, // b a b a g#
    349, 294, 349, 311, // f d f Eb
    392, 440, 392, 311, // g a g Eb
    349, 294, 349, 311, // f d f Eb
    392, 440, 392, 311, // g a g Eb
    349, 294, 349, 311, // f d f Eb
    349, 392, 349, 277, // f g f Db
    349 // f
  ];
  var freq2 = [
    330, 311, 330, 311, 277, // e d# e d# e c#
    622, 523, 622, 523, // Eb c Eb c
    622, 554, 622, 554, // Eb Db Eb Db
    622, 523, 622, 523, // Eb c Eb c
    622, 554, 622, 554, // Eb Db Eb Db
    622, 523, 622, 523, // Eb c Eb c
    554, 523, 554, 523, // Db c Db c
    466 // Bb
  ];
  var durs = [
    2, 2, 2, 2, 6,
    2, 2, 2, 2,
    2, 2, 2, 2,
    2, 2, 2, 2,
    2, 2, 2, 2,
    2, 2, 2, 2,
    2, 2, 2, 2,
    8
  ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \duoPad,
    \group, g,
    \addAction, 0,
    \outBus, pBus,
    \dur, Pseq(durs, 1),
    \freq1, Pseq(freq1, 1),
    \freq2, Pseq(freq2, 2),
    \amp, 0.4
  ).play(clock);
  Synth.tail(g, \brawny, [ sinFreq: 0.3, inBus: pBus, outBus: 0, fundamental: 207 * 2 ]);
)

