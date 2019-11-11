(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
Array.fill(7, { |n| (n + 1).squared + 1}).postln;
(
  ~scuttleBuf = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff/scuttle.wav");
  SynthDef(\granulate, { | buf |
    var mg, delay1, delay2, mix;
    var winsizes = Array.fill(7, { |n| (n + 1) * 0.004 }).reverse;
    var grainrates = Array.fill(7, { |n| (n + 1).squared + 1});
    // grainrates = grainrates.reverse ++ grainrates;
    grainrates = [10, 2, 6] / 2;
    winsizes = winsizes.reverse ++ winsizes;
    winsizes = [ 0.01, 0.002, 0.05 ];
    winsizes = [ 0.035, 0.052, 0.035, 0.017 ] / 2;
    winsizes.postln;
    mg = MonoGrain.ar(
      in: PlayBuf.ar(1, buf, loop: 0),
      winsize: winsizes,
      grainrate: grainrates,
      winrandpct: 0.12,
      mul: 0.1
    );
    mix = Mix(mg);
    delay1 = DelayN.ar(mix, 0.6666, 0.6666);
    delay2 = DelayN.ar(mix, 1.33333, 1.3333);
    Out.ar(
      0, Splay.ar([mix - delay1, mix, mix - delay2]);
    );
  }).add;
)
Synth(\granulate, [buf: ~scuttleBuf]);

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
  SynthDef(\bassgrowl, { | freq1 = 55, freq2 = 58, cutoffLow = 100, cutoffHigh = 1024 |
    var thurk;
    thurk = RLPF.ar(
      in: LFSaw.ar(
        freq: [freq1, freq2],
        iphase: [0, 1.3],
        mul: EnvGen.kr(
          Env.perc(attackTime: 0.05, releaseTime: 0.2, curve: 2.7),
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
      rq: 0.4,
      mul: 0.5,
      add: 0.0
    );
    Out.ar(0, thurk);
  }).add;
)

Synth(\lowpong);
Synth(\bassgrowl);

(
  var clock = TempoClock(90/60);
  var durs = [
    0.1, 0.5, 0.5, 0.5, 0.4,
    0.1, 0.3333, 0.3333, 0.33333,
    0.3333, 0.3333, 0.2333333
  ];
  var tensions = [ 
    0.1, 0.13, 0.1, 0.1, 0.1,
    0.1, 0.13, 0.1, 0.1,
    0.13, 0.1, 0.1
  ];
  var highCutoffs = [
    718, 1024, 512, 512, 512,
    718, 1024, 512, 512,
    718, 512, 512
  ];
  Pbind(
    \instrument, \lowpong,
    \dur, Pseq(durs, 1),
    \tension, Pseq(tensions, 1)
  ).play(clock);
  Pbind(
    \instrument, \bassgrowl,
    \dur, Pseq(durs, 1),
    \cutoffLow, 50,
    \cutoffHigh, Pseq(highCutoffs, 1)
  ).play(clock);
)

(
  SynthDef(\dustOfTime, { 
    arg density = 512, dur = 2, amp = 0.5;
    var dustEnv = XLine.kr(start: density, end: 12, dur: dur, doneAction: Done.freeSelf) ! 4;
    var dustAmp = Array.fill(4, amp).postln;
    var dust = Dust.ar(density: dustEnv, mul: dustAmp);
    Out.ar(
      0,
      Splay.ar(dust);
    );
  }).add;
)
(
  var clock = TempoClock(90/60);
  var densities = [
    512, 256, 128, 64
  ];
  var amps = [
    0, 0, 0.5,
    0, 0.3, 0,
    0, 0, 0
  ];
  var dWeights = [
    0.3, 0.2, 0.4, 0.1
  ];
  Pbind(
    \instrument, \dustOfTime,
    \amp, Pseq(amps, inf),
    \dur, 1,
    \density, Pwrand(densities, dWeights, inf)
  ).play(clock);
  Pbind(
    \instrument, \lowpong,
    \dur, 1,
    \amp, Pseq(amps, inf)
  ).play(clock);
)

(
  SynthDef(\pluck, { | freq = 220, amp = 0.5, gate = 1, pan = 0, c3 = 20, c1 = 10 |
    var env = Env.new(
      levels: [0, 1, 1, 0], 
      times: [0.001, 0.006, 0.0005], 
      curve: [5, -5, -8]
    );
    var inp = amp * LFClipNoise.ar(2000) * EnvGen.ar(env, gate);
    var pluck = DWGPlucked.ar(
      freq: freq,
      amp: amp, 
      gate: gate, 
      pos: 0.1, 
      c1: c1, 
      c3: c3,
      inp: inp
    );
    DetectSilence.ar(pluck, 0.001, doneAction: Done.freeSelf);
    Out.ar(0, Pan2.ar(pluck * 0.1, pan));
  }).add;
)

Synth(\pluck);

(
  var clock = TempoClock(90/60);
  var cDorian = [
    1175, 1244, 1244, 1047, 1047, 1397 // d Eb Eb c c f
  ];
  var cQuartal = [
    1244, 1397, 1397, 1047, 1047, 1661 // Eb f f c c Ab
  ];
  var aQuartal = [
    1047, 1244, 1244, 880, 880, 1397 // c Eb Eb a a f
  ];
  var bbQuartal = [
    1109, 1244, 1244, 932, 932, 1480 // Db Eb Eb Bb Gb
  ];
  var cDorian2 = [
    1175, 1244, 1244, 1047, 1047, 1568 // d Eb Eb c c g
  ];
  var cMinor = [
    1175, 1244, 1244, 1047, 1047, 1661 // d Eb Eb c c Ab
  ];
  var seq1 = cDorian ++ cDorian ++ cDorian ++
    cQuartal ++ cQuartal ++ cQuartal ++
    aQuartal ++ aQuartal ++ aQuartal ++
    bbQuartal ++ bbQuartal ++ bbQuartal ++
    cDorian2 ++ cDorian2 ++ cDorian2 ++
    cMinor ++ cMinor ++ cMinor;
  var ebF = [ 1244, 1397, 1397, 1244, 1244, 1397 ];
  var ebG = [ 1244, 1568, 1568, 1244, 1244, 1568 ];
  var ebAb = [ 1244, 1661, 1661, 1244, 1244, 1661 ];
  var seq1alt = ebF ++ ebF ++ ebF ++
    ebF ++ ebF ++ ebF ++
    ebF ++ ebF ++ ebF ++
    ebF ++ ebF ++ ebF ++
    ebG ++ ebG ++ ebG ++
    ebAb ++ ebAb ++ ebAb;
  var seq2 = [
    1397, 1397, 932, // f f Bb
    1760, 1865, 1865, 1568, 1568, 1244, // a Bb Bb g g Eb
    1568, 1760, 1760, 1397, 1397, 932, // g a a f f Bb
    1760, 1865, 1865, 1568, 1568, 1244, // a Bb Bb g g Eb
    1568, 1760, 1760, 1397, 1397, 932, // g a a f f Bb
    1760, 1865, 1865, 1568, 1568, 1244, // a Bb Bb g g Eb
    1568, 1760, 1760, 1397, 1397 // g a a f f
  ];
  var seq2alt = [ 1568, 1568, 1760 ] ++ [ 1568, 1760, 1760, 1568, 1568, 1760 ].wrapExtend(35);
  Pbind(
    \instrument, \pluck,
    \freq, Pseq(seq2alt, 1),
    \amp, Pwhite(0.3, 0.6),
    \pan, Pwhite(-0.7, 0.7),
    \c1, Pwhite(5, 10),
    \dur, 1/2,
    \c3, Pwhite(1200, 3200)
  ).play(clock);
)

(
  var seq2alt = [ 1568, 1568, 1760 ] ++ [ 1568, 1760, 1760, 1568, 1568, 1760 ].wrapExtend(11);
  seq2alt.postln;
)

(
  SynthDef(\klankNoise, {
    var wn = WhiteNoise.ar(0.1);
    var wnF = OneZero.ar(wn, Line.kr(0.01, 1, 7) - 0.5);
    var wnEnv = Env.perc(0.01, 7).kr(2) ! 4;
    var wnOut = wnF * wnEnv;
    var dustEnv = XLine.kr(start: 512, end: 1, dur: 7, doneAction: Done.freeSelf) ! 4;
    var klank = Klank.ar(`[[349, 523, 1397, 1866], [1, 0.8, 0.6, 0.5], [3, 2, 1, 4]], Dust.ar(dustEnv, 0.1));
    var mix = Mix([ wnOut, klank ]);
    Out.ar(0, Splay.ar(mix));
  }).add;
)

Synth(\klankNoise);

(
  SynthDef(\granPlayer, { | out = 0, buf = 0, gate = 1, level = 0.5, dur = 1 |
    var signal, filter;
    // var env = Linen.kr(gate: gate, releaseTime: 0.1, susLevel: level, doneAction: Done.freeSelf);
    var env = EnvGen.kr(Env.perc(0.01, dur * 0.8), 1.0, levelScale: level, doneAction: Done.freeSelf);
    signal = PlayBuf.ar(numChannels: 1, bufnum: buf, rate: BufRateScale.kr(buf));
    signal = signal * env;
    filter = GlitchRHPF.ar(signal, 742, 0.4, Saw.kr(0.5, 1.3));
    Out.ar(out, filter ! 2);
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
  var clock = TempoClock(90/60);
  var durs = [
    1, 1, 1, 0.5
  ] / 2;
  Pbind(
    \instrument, \granPlayer,
    \dur, Pseq(durs, inf),
    \buf, Prand(~ramblingEighths, inf)
  ).play(clock);
)

// Tests

(
  {
    GlitchHPF.ar(
      BrownNoise.ar(0.1.dup),
      0.1,
      Saw.kr(1, 10)
    )
  }.play;
)

Env.perc(attackTime: 0.05, releaseTime: 0.2, curve: 2.7).plot;
{ LFCub.kr(0.1, 0.5 * pi) }.plot;
(
  {
    RLPF.ar(
      in: LFSaw.ar(
        freq: [55, 58],
        iphase: [0, 1.3],
        mul: EnvGen.kr(
          Env.perc(attackTime: 0.05, releaseTime: 0.2, curve: 2.7),
          // Env.asr(0.5, 1, 0.02),
          1.0,
          doneAction: Done.freeSelf
        ) * 0.2
      ),
      freq: LinExp.kr(
        Line.kr(0.1, 0.9, 0.15),
        -1,
        1,
        100,
        1024
      ),
      // freq: 400,
      rq: 0.2,
      mul: 0.1,
      add: 0.0
    )
  }.play
)
{ OneZero.ar(WhiteNoise.ar(0.5), MouseX.kr(-0.49, 0.49)) }.scope(1);
(
  { 
    var dustEnv = XLine.kr(start: 512, end: 1, dur: 7, doneAction: Done.freeSelf) ! 4;
    Klank.ar(`[[349, 523, 1397, 1866], [1, 0.8, 0.6, 0.5], [3, 2, 1, 4]], Dust.ar(dustEnv, 0.1)) 
  }.play;
)
