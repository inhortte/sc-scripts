(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\highPerc, {
    arg outBus = 0, freq = 220, dur = 1, durMult = 0.8, amp = 0.1;
    var env = Env.perc(0.01, durMult * dur, curve: -3).kr(2);
    var noise = PinkNoise.ar(amp) * env;
    var klang = Klank.ar(
      `[
        [ freq, freq * 1.02, freq * 0.98 ],
        [ amp, amp * 0.9, amp * 0.8 ],
        [ dur * 0.2, dur * 0.18, dur * 0.22 ]
      ],
      noise
    ) * env;
    Out.ar(outBus, Splay.ar(klang));
  }).add;
)
Synth(\highPerc, [ freq: 2000, dur: 1, amp: 0.1 ]);
(
  SynthDef(\harpsichord, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var noiseEnv = Env.perc(0.01, 0.8 * dur, curve: -3).kr(2);
    var noise = RLPF.ar(
      in: PinkNoise.ar(amp),
      freq: SinOsc.kr(0.7).range(0.7, 1.3) * [ freq * 3, freq * 7 ],
      rq: 0.1
    );
    var maxdelaytimes = [freq, freq * 1.01, freq * 0.99 ].reciprocal;
    var delaytimes = maxdelaytimes;
    var pluck = Pluck.ar(
      in: noise,
      trig: noiseEnv,
      maxdelaytime: maxdelaytimes,
      delaytime: delaytimes,
      decaytime: dur * 1.2,
      coef: SinOsc.kr(0.3).range(-0.7, 0.7)
    );
    Out.ar(outBus, Splay.ar(pluck));
  }).add;
)
Synth(\harpsichord, [ freq: 880, dur: 1, amp: 0.1 ]);

// Chorus 1 (C aeolean)
(
  var harpMelody = [ 1, 2, 0, 4, 5 ];
  var harpDur = [ 0.5, 0.5, 1, 1, 0.5 ];
  var cAeolean = [ 0, 2, 3, 5, 7, 8, 10 ];
  var harpMelodyBind = Pbind(
    \instrument, \harpsichord,
    \octave, 5,
    \ctranspose, 1,
    \scale, cAeolean,
    \degree, Pseq(harpMelody, 4),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harpHarmOne = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \ctranspose, 1,
    \scale, cAeolean,
    \degree, Pseq([ 5 ], 20),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.07, 0.08, 0.05 ], inf)
  );
  var harpMelodyTwoBind = Pbind(
    \instrument, \harpsichord,
    \octave, 5,
    \ctranspose, 3,
    \scale, cAeolean,
    \degree, Pseq(harpMelody, 2),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harpHarmTwo = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \ctranspose, 3,
    \scale, cAeolean,
    \degree, Pseq([ 5 ], 10),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.07, 0.08, 0.05 ], inf)
  );
  Ptpar([ 
    0.0, harpMelodyBind, 
    0.0, harpHarmOne,
    14, harpMelodyTwoBind,
    14, harpHarmTwo
  ]).play(TempoClock(80 / 60));
)
// D harmonic minor
(
  var harpMelody = [ 1, 2, 0, 4, 5 ];
  var harpDur = [ 1, 1.5, 1, 1.5, 1 ];
  var dHarmMin = [ 2, 4, 5, 7, 9, 10, 13 ];
  var harpMelodyBind = Pbind(
    \instrument, \harpsichord,
    \octave, 5,
    \scale, dHarmMin,
    \degree, Pseq(harpMelody, inf),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harpHarm = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \scale, dHarmMin,
    \degree, Pseq([ Pshuf([ 2, 3, 6 ], 3), Pseq([ 1, 0, 2 ], 3) ], inf),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.07, 0.08, 0.05 ], inf)
  );
  var highPerc = Pbind(
    \instrument, \highPerc,
    \octave, 6,
    \scale, dHarmMin,
    \degree, Prand([ 2, 3, 2, 2, 3, \ ], inf),
    \dur, 2,
    \durMult, Prand([ 0.8, 0.6, 0.4, 0.5, 0.76, 0.35, 0.45 ], inf),
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  Ppar([ harpMelodyBind, harpHarm ]).play(TempoClock(80/60));
)
// E harmonic major / C
(
  var harpMelody = [ 1, 2, 0, 4, 5 ];
  var harpDur = [ 1, 1.5, 1, 1.5, 1 ];
  var eHarmMajOverC = [ 0, 3, 4, 6, 8, 9, 11 ];
  var harpMelodyBind = Pbind(
    \instrument, \harpsichord,
    \octave, 5,
    \scale, eHarmMajOverC,
    \degree, Pseq(harpMelody, inf),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harpHarm = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \scale, eHarmMajOverC,
    \degree, Pseq([ Pshuf([ 2, 3, 6 ], 3), Pseq([ 1, 0, 2 ], 3) ], inf),
    \dur, Pseq(harpDur, inf),
    \amp, Prand([ 0.07, 0.08, 0.05 ], inf)
  );
  var highPerc = Pbind(
    \instrument, \highPerc,
    \octave, 6,
    \scale, eHarmMajOverC,
    \degree, Prand([ 2, 3, 2, 2, 3, \ ], inf),
    \dur, 2,
    \durMult, Prand([ 0.8, 0.6, 0.4, 0.5, 0.76, 0.35, 0.45 ], inf),
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  Ppar([ harpMelodyBind, harpHarm ]).play(TempoClock(80/60));
)
// intro
(
  var harpHigh = [ 5, 6 ];
  var harpLow = [ 7 ];
  var dMelMinor = [ 2, 4, 5, 7, 9, 11, 13 ];
  var harpHighBind = Pbind(
    \instrument, \harpsichord,
    \octave, 4,
    \scale, dMelMinor,
    \degree, Pseq(harpHigh, 16),
    \dur, 2,
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harpLowBind = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \scale, dMelMinor,
    \degree, Pseq(harpLow, 32),
    \dur, 2,
    \amp, Prand([ 0.07, 0.08, 0.05 ], inf)
  );
  var harpLowEnd = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \scale, dMelMinor,
    \degree, Pseq([7, 7, 7], 1),
    \dur, 0.5,
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  Ptpar([ 
    0.0, harpHighBind, 
    0.0, harpLowBind,
    64, harpLowEnd
  ]).play(TempoClock(80/60));
)
// outro
(
  var harpHigh = [ 5, 6 ];
  var harpLow = [ 7 ];
  var bMinor = [ -1, 1, 2, 4, 6, 7, 9 ];
  var harpHighBind = Pbind(
    \instrument, \harpsichord,
    \octave, 4,
    \scale, bMinor,
    \degree, Pseq(harpHigh, 16),
    \dur, 2,
    \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harpLowBind = Pbind(
    \instrument, \harpsichord,
    \octave, 3,
    \scale, bMinor,
    \degree, Pseq(harpLow, 32),
    \dur, 2,
    \amp, Prand([ 0.07, 0.08, 0.05 ], inf)
  );
  Ptpar([ 
    0.0, harpHighBind, 
    0.0, harpLowBind
  ]).play(TempoClock(80/60));
)
// Transition 1
(
  var aMelMinor = [ 9, 11, 12, 14, 16, 18, 20 ];
  var aMajor = [ 9, 11, 13, 14, 16, 18, 20 ];
  var harp1PartOne = Pbind(
    \instrument, \harpsichord, \octave, 3, \scale, aMelMinor, \degree, Pseq([ 7, 7, 7, 7 ], 1), \dur, 2, \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harp1PartTwo = Pbind(
    \instrument, \harpsichord, \octave, 4, \scale, aMajor, \degree, Pseq([ 1, 1 ], 1), \dur, 2, \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harp1PartThree = Pbind(
    \instrument, \harpsichord, \octave, 3, \scale, aMelMinor, \degree, Pseq([ 7, 7 ], 1), \dur, 2, \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harp2PartOne = Pbind(
    \instrument, \harpsichord, \octave, 4, \scale, aMelMinor, \degree, Pseq([ 1, 1, 1, 1 ], 1), \dur, 2, \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harp2PartTwo = Pbind(
    \instrument, \harpsichord, \octave, 4, \scale, aMajor, \degree, Pseq([ 0, 0 ], 1), \dur, 2, \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  var harp2PartThree = Pbind(
    \instrument, \harpsichord, \octave, 4, \scale, aMelMinor, \degree, Pseq([ 2, 2 ], 1), \dur, 2, \amp, Prand([ 0.12, 0.09, 0.1, 0.11 ], inf)
  );
  Ptpar([ 
    0.0, harp1PartOne, 
    0.0, harp2PartOne,
    8, harp1PartTwo,
    8, harp2PartTwo,
    12, harp1PartThree,
    12, harp2PartThree
  ]).play(TempoClock(80/60));
)
