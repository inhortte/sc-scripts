(
  s.options.numBuffers = 8192; 
  s.options.numWireBufs = 1024;
  s.boot;
)
s.plotTree;
(
  SynthDef(\sawMyFaceOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1, sweepLow = 554, sweepHigh = 1108;
    var env = Env.perc(0.01, 0.8 * dur, curve: -1).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freq,
      iphase: 1.2
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: SinOsc.kr(1 / 2.86).range(554, 1108),
      freq: LFNoise1.kr(2.86, sweepLow, sweepHigh),
      rq: 0.2
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, Splay.ar(allPass));
  }).add;
)
Synth(\sawMyFaceOff, [ freq: 440, dur: 2, amp: 0.2 ]);
[0.5, 1, 2] *.t [2794, 3729];
(
  SynthDef(\duoPad, {
    arg outBus = 0, freq1 = 622, freq2 = 830, dur = 1, amp = 0.1;
    var env = EnvGen.ar(
      Env.new([0, 1, 0.6, 0], [dur * 0.178, 0.536 * dur, dur * 0.357], curve: [-0.5, 3, 2]),
      levelScale: amp,
      doneAction: Done.freeSelf
    );
    var formants = Formants.ar(
      baseFreq: ([0.5, 1, 2] *.t [freq1, freq2] * {LFNoise1.kr(10, 0.003, 1)}!4).flat,
      vowel: Vowel([\i, \o], [\bass, \tenor]),
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
Synth(\duoPad, [ freq1: 2794, freq2: 3729, dur: 2, amp: 0.7 ]);
(
  Synth(\duoPad, [ freq1: 3951, freq2: 2794, dur: 7, amp: 0.7 ]);
  Synth(\sawMyFaceOff, [ freq: 1480 / 2, dur: 8, amp: 0.1, sweepLow: 1397 / 2, sweepHigh: 5588 / 2 ]);
)
