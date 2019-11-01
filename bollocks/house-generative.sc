(
  s.options.numBuffers = 8192; 
  s.options.numWireBufs = 1024;
  s.boot;
)
s.plotTree;
Quarks.gui

(
  SynthDef(\bass, {
    arg outBus = 0, sinFreq = 1, fundamental = 220, amp = 0.1, dur = 1;
    var env = Env.perc(0.02, dur * 0.3, curve: -2).kr(2);
    var saw = VarSaw.ar(
      freq: fundamental,
      iphase: pi / 2,
      width: SinOsc.kr(sinFreq, pi / 2).range(0.4, 0.6),
      mul: amp
    ) * env;
    var lpf = LPF.ar(
      in: saw,
      freq: SinOsc.kr(sinFreq * 2, pi / 4).range(fundamental * 5, fundamental * 7)
    );
    var membrane = MembraneHexagon.ar(
      lpf,
      tension: 0.08,
      loss: XLine.kr(0.99, 0.99999, dur * 0.6)
    );
    Out.ar(outBus, membrane ! 2);
  }).add;
  SynthDef(\bustle, {
    arg outBus = 0, freq1 = 123, freq2 = 185;
    var klank = Klank.ar([
      `[[freq1, freq2 * 2], nil, [1, 1]],
      `[[freq2, freq1 * 2], nil, [1, 1]]
    ], PinkNoise.ar(0.01));
    Out.ar(outBus, klank);
  }).add;
  SynthDef(\step, {
    arg inBus = 7, outBus = 0, dur = 1, amp = 1;
    var env = Env.perc(0.04, dur * 0.2, curve: -4).kr(2);
    var input = In.ar(inBus, 2) * env * amp;
    var delay = DelayN.ar(
      in: input * 0.7, 
      maxdelaytime: 1.0,
      delaytime: 0.3,
      mul: 0.5,
      add: input
    );
    Out.ar(outBus, delay);
  }).add;
  SynthDef(\noise, {
    arg outBus = 0, amp = 0.1;
    var noise = PinkNoise.ar(amp);
    Out.ar(outBus, noise ! 2);
  }).add;
)
Env.new([0, 1, 0.8, 0], [8 * 0.375, 0.625 * 8, 8 * 0.25], curve: [-0.5, 3, 2]).plot;
(
  SynthDef(\triadPad, {
    arg outBus = 0, freq1 = 622, freq2 = 830, freq3 = 1109, dur = 1, amp = 0.1;
    var env = EnvGen.ar(
      Env.new([0, 1, 0.8, 0], [dur * 0.375, 0.625 * dur, dur * 0.25], curve: [-0.5, 3, 2]),
      levelScale: amp,
      doneAction: Done.freeSelf
    );
    var formants = Formants.ar(
      baseFreq: ([1, 2, 3 ,4] *.t [freq1, freq2, freq3] * {LFNoise1.kr(10, 0.003, 1)}!4).flat,
      vowel: Vowel([\u, \e], [\bass, \tenor, \soprano]),
      freqMods: LFNoise2.ar(4 * [0.1, 0.2, 0.3, 0.4, 0.5].scramble, 0.1),
      ampMods: env
    );
    var reverb = FreeVerb.ar(
      in: Splay.ar(formants.flat.scramble),
      mix: 0.4,
      room: 0.3,
      damp: 0.8
    );
    Out.ar(outBus, reverb);
  }).add;
)
(
  var clock = TempoClock(100 / 60);
  var stepDurs = [ 3, 3.03125 ];
  var nBus = Bus.audio(s, 2);
  var bBus1 = Bus.audio(s, 2);
  var bBus2 = Bus.audio(s, 2);
  var bBus3 = Bus.audio(s, 2);
  var s1, s2, s3;
  g = Group.basicNew(s, 1);
  b = Synth.head(g, \noise, [ outBus: nBus, amp: 0.2 ]);
  s1 = Synth.head(g, \bustle, [ outBus: bBus1, freq1: 247, freq2: 370 ]);
  s2 = Synth.head(g, \bustle, [ outBus: bBus2, freq1: 277, freq2: 370 ]);
  s3 = Synth.head(g, \bustle, [ outBus: bBus3, freq1: 165, freq2: 370 ]);
  Pbind(
    \instrument, \bass,
    \group, g,
    \addAction, 0,
    \fundamental, 46,
    \dur, 6,
    \amp, Prand([0.7, 0.8, 0.85], inf)
  ).play(clock);
  Pbind(
    \instrument, \step,
    \group, g, // s3,
    \addAction, 1,
    \inBus, Prand([ bBus1, bBus2, bBus3 ], inf),
    // \inBus, nBus,
    \dur, Pseq(stepDurs, inf),
    \amp, Prand([ 0.9, 0.8, 0.85 ], inf)
  ).play(clock);
)
(
  var ebQuartal1 = Array.fill(3, { [622, 830, 1109, 1480, 1976].choose }); // Eb quartal
  var ebQuartal2 = Array.fill(3, { [622, 830, 1109, 1480, 1976].choose }); // Eb quartal
  var ebQuartal3 = Array.fill(3, { [622, 830, 1109, 1480, 1976].choose }); // Eb quartal
  var bQuartal1 = Array.fill(3, { [659, 880, 1175, 1568, 1976].choose }); // B quartal (/ E)
  var bQuartal2 = Array.fill(3, { [659, 880, 1175, 1568, 1976].choose }); // B quartal (/ E)
  var bQuartal3 = Array.fill(3, { [659, 880, 1175, 1568, 1976].choose }); // B quartal (/ E)
  var bbDim1 = Array.fill(3, { [659, 932, 1109, 1568].choose }); // Bb dim7
  var bbDim2 = Array.fill(3, { [659, 932, 1109, 1568].choose }); // Bb dim7
  var bbDim3 = Array.fill(3, { [659, 932, 1109, 1568].choose }); // Bb dim7
  var fisQuartal1 = Array.fill(3, { [740, 988, 1319, 1760, 2349].choose }); // F# quartal
  var fisQuartal2 = Array.fill(3, { [740, 988, 1319, 1760, 2349].choose }); // F# quartal
  var fisQuartal3 = Array.fill(3, { [740, 988, 1319, 1760, 2349].choose }); // F# quartal
  var firstTone = [ ebQuartal1.at(0), ebQuartal2.at(0), ebQuartal3.at(0),
    bQuartal1.at(0), bQuartal2.at(0), bQuartal3.at(0),
    bbDim1.at(0), bbDim2.at(0), bbDim3.at(0),
    fisQuartal1.at(0), fisQuartal2.at(0), fisQuartal3.at(0) ];
  var secondTone = [ ebQuartal1.at(1), ebQuartal2.at(1), ebQuartal3.at(1),
    bQuartal1.at(1), bQuartal2.at(1), bQuartal3.at(1),
    bbDim1.at(1), bbDim2.at(1), bbDim3.at(1),
    fisQuartal1.at(1), fisQuartal2.at(1), fisQuartal3.at(1) ];
  var thirdTone = [ ebQuartal1.at(2), ebQuartal2.at(2), ebQuartal3.at(2),
    bQuartal1.at(2), bQuartal2.at(2), bQuartal3.at(2),
    bbDim1.at(2), bbDim2.at(2), bbDim3.at(2),
    fisQuartal1.at(2), fisQuartal2.at(2), fisQuartal3.at(2) ];
    /*
  var triads = Prand([ ebQuartal1, ebQuartal2, ebQuartal3, bQuartal1, bQuartal2, bQuartal3, bbDim1, bbDim2, bbDim3, fisQuartal1, fisQuartal2, fisQuartal3 ]).repeat(24).asStream;
  */
  Pbind(
    \instrument, \triadPad,
    \freq1, Pseq(firstTone, inf),
    \freq2, Pseq(secondTone, inf),
    \freq3, Pseq(thirdTone, inf),
    \amp, 0.5,
    \dur, 8
  ).play(TempoClock(100/60));
)

// Tests

(
  SynthDef(\route, {
    arg inBus = 7, outBus = 0;
    Out.ar(outBus, In.ar(inBus, 2));
  }).add;
)
(
  var bBus1 = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Synth.head(g, \bustle, [ outBus: bBus1, freq1: 123, freq2: 185 ]);
  Synth.tail(g, \step, [ inBus: bBus1, dur: 2, amp: 0.5 ])
  // Synth.tail(g, \route, [ outBus: 0, inBus: bBus1 ]);
)
Synth(\bass, [ fundamental: 46, sinFreq: 2, amp: 0.8, dur: 1 ]);
Synth(\bustle);
// 
play({
    VarSaw.ar(
        LFPulse.kr(3, 0, 0.3, 200, 200),
        0,
        LFTri.kr(1.0).range(0,1), //width
        0.1)
});
{ Klank.ar(`[[123, 185], nil, [1, 1]], Dust.ar(16, 0.1)) }.play;
{LFNoise2.ar(1 * [0.1, 0.2, 0.3, 0.4, 0.5].scramble, 0.1)}.plot;
(
  Ndef(\pad, {
    Formants.ar(
      baseFreq: ([1, 2, 3 ,4] *.t [31, 50].midicps * {LFNoise1.kr(10, 0.003, 1)}!4).flat,
      vowel: Vowel([\u, \e], [\bass, \tenor, \soprano]),
      freqMods: LFNoise2.ar(5 * [0.1, 0.2, 0.3, 0.4, 0.5].scramble, 0.1),
      ampMods: 0.04
    );
  });
)
(
  Ndef(\pad).fadeTime = 10;

  (
    Ndef(\pad, {
      var src;
      src = Formants.ar(([1, 2, 3, 4] *.t [31, 50].midicps * {LFNoise1.kr(10, 0.003, 1)}!4).flat, Vowel([\u, \e], [\bass, \tenor, \soprano]), 
      freqMods: LFNoise2.ar(1*[0.1, 0.2, 0.3, 0.4, 0.5].scramble, 0.1), unfold: true).sum * 0.1;
      Splay.ar(src.flat.scramble)
    }).play
  )
)
(
  Ndef(\sound).play;
  Ndef(\sound).fadeTime = 5;
  Ndef(\sound, { SinOsc.ar([600, 635], 0, SinOsc.kr(2).max(0) * 0.2) });
  Ndef(\sound, { SinOsc.ar([600, 635] * 3, 0, SinOsc.kr(2 * 3).max(0) * 0.2) });
  Ndef(\sound, { SinOsc.ar([600, 635] * 2, 0, SinOsc.kr(2 * 3).max(0) * 0.2) });
  Ndef(\sound, Pbind(\dur, 0.17, \freq, Pfunc({ rrand(300, 700) })) );

  Ndef(\lfo, { LFNoise1.kr(3, 400, 800) });
  Ndef(\sound).map(\freq, Ndef(\lfo));
  Ndef(\sound, { arg freq; SinOsc.ar([600, 635] + freq, 0, SinOsc.kr(2 * 3).max(0) * 0.2) });
  /*
  Ndef(\lfo, { LFNoise1.kr(300, 400, 800) });

  Ndef.clear; //clear all Ndefs)
  */
)

(
  Ndef(\click).fadeTime = 10;
  Ndef(\click, {
    var src, in;
    var mods = LFNoise2.ar(#[0.1, 0.2, 0.3, 0.4, 0.5].scramble 
    * {LFNoise1.ar(0.1).range(0.125, 4)}!5, 0.01);

    in = Impulse.ar(75 * [1, 0.66, 0.75, 2.33, 0.2387], mul: 1);
    src = Vowel([\a, \o, \e, \i, \u].scramble, [\bass, \bass, \soprano])
    .collect{|vowel, i|
      BPFStack.ar(
        (PinkNoise.ar * 0.0125) + 
        in[i], 
        vowel,
        freqMods: mods[i] + 1.1
      )};
      Splay.ar(src.flat.scramble)
    }).play
)

