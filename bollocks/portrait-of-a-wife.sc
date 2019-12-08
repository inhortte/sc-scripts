(
  s.options.numBuffers = 8192; 
  s.options.numWireBufs = 1024;
  s.boot;
)
s.plotTree;
(
  SynthDef(\granPlayer, { | out = 0, buf = 0, gate = 1, level = 0.5, dur = 1 |
    var signal, filter;
    // var env = Linen.kr(gate: gate, releaseTime: 0.1, susLevel: level, doneAction: Done.freeSelf);
    var env = EnvGen.kr(Env.perc(0.01, dur * 0.8), 1.0, levelScale: level, doneAction: Done.freeSelf);
    signal = PlayBuf.ar(numChannels: 1, bufnum: buf, rate: BufRateScale.kr(buf));
    signal = signal * env;
    // filter = GlitchRHPF.ar(signal, 742, 0.4, Saw.kr(0.5, 1.3));
    Out.ar(out, signal ! 2);
  }).add;
)

(
  s.freeAllBuffers;
  ~ramblingQuarters = "/home/polaris/flavigula/xian/granules-of-ramblings-to-krzys-quarters/*.wav".pathMatch.collect { |file|
    Buffer.read(s, file);
  };
  ~ramblingEighths = "/home/polaris/flavigula/xian/granules-of-ramblings-to-krzys-eighths/*.wav".pathMatch.collect { |file|
    Buffer.read(s, file);
  };
)
(
  ~goatHatey = Buffer.read(s, "/home/polaris/flavigula/xian/babblings/goat-hatey-snip.wav");
)

(
  var clock = TempoClock(84/60);
  var durs = [
    2, 0.5, 0.5, 3, 0.5, 5.5
  ];
  var amps = [ 0, 1, 0.7, 0, 0.8, 0 ];
  Pbind(
    \instrument, \granPlayer,
    \dur, Pseq(durs, inf),
    \level, Pseq(amps, inf),
    \buf, ~goatHatey
    // \buf, Prand(~ramblingEighths, inf)
  ).play(clock);
)

(
  SynthDef(\sawMyFaceOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var env = Env.perc(0.01, 0.8 * dur, curve: -3).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freq,
      iphase: 1.2
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: SinOsc.kr(1 / 2.86).range(554, 1108),
      freq: LFNoise1.kr(2.86, 554, 1108),
      rq: 0.4
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, lpf ! 2);
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
  var dur = 1.42857; // half note at 84bpm
  var freqs = Routine.new({
    loop({
      #[2794, 3322, 3729, 4435, 4978].choose.yield;
    });
  });
  var amps = Routine.new({
    loop({
      var amp = #[ 0.2, 0.17, 0.23, 0.18, 0.22 ].choose * 4;
      amp.yield;
    });
  });
  Routine({
    loop({
      var freq1 = freqs.next;
      var freq2 = freqs.next;
      /*
      var freq3 = freqs.next / 2;
      var freq4 = freqs.next / 2;
      */
      Synth(\duoPad, [ freq1: freq1, freq2: freq2, amp: amps.next, dur: dur ]);
      // Synth(\duoPad, [ freq1: freq3, freq2: freq4, amp: amps.next / 2, dur: dur ]);
      dur.wait;
    });
  }).play;
)
