(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

// Bass drum
(
  SynthDef(\thomp, {
    arg outBus = 0, freq = 69, ratio = 3, sweeptime = 0.05, preamp = 1, amp = 0.1, decayL = 0.8, dur = 1, decay1Base = 0.3, decay2Base = 0.15;
    var decay1 = decay1Base * dur, decay2 = decay2Base * dur;
    var fEnv = EnvGen.kr(
      Env.new([ freq * ratio, freq ], [ sweeptime ], \exp)
    );
    var env = EnvGen.kr(
      Env.new([0, 1, decayL, 0], [0.01, decay1, decay2], -3),
      doneAction: Done.freeSelf
    );
    var signal = SinOsc.ar(fEnv, 0.5pi, preamp).distort * env * amp;
    Out.ar(outBus, signal ! 2);
  }).add;
)

// high hat
(
  SynthDef(\snare, {
    arg outBus = 0, freq = 1661, rq = 3, decay = 0.4, pan = 0, amp = 0.1;
    var env = Env.perc(0.01, decay).kr(2);
    var signal = PinkNoise.ar(amp);
    var filter = BPF.ar(signal, freq, rq) * env;
    Out.ar(outBus, Pan2.ar(filter, pan));
  }).add;
)

Synth(\thomp, [ freq: 69, amp: 0.5, decay1Base: 0.1, decay2Base: 0.2, ratio: 4, sweeptime: 0.08 ]);
Synth(\snare, [ freq: 3729, amp: 12, decay: 0.04, rq: 0.06 ]);
(
  ~hhAmps = Array.fill(512, {
    arg n;
    var baseAmp = 17;
    var leper = case
      { n.mod(5) == 0 } { 0 }
      { n.mod(7) == 0 } { baseAmp * 0.25 }
      { n.mod(3) == 0 } { baseAmp * 0.7 }
      { n == n } { baseAmp };
    leper;
  });
)
(
  var clock = TempoClock(84/60);
  var thompDurs = [1, 1, 0.5, 1.5];
  var thompAmps = [0.5, 0.4, 0.4, 0.3];
  Pbind(
    \instrument, \thomp,
    \dur, Pseq(thompDurs, inf),
    \amp, Pseq(thompAmps, inf),
    \freq, 69, // Db,
    \decayL, Prand([ 0.8, 0.6, 0.7, 0.83, 0.5 ], inf),
    \decay1Base, Prand([ 0.1, 0.08, 0.11, 0.09 ], inf),
    \decay2Base, Prand([ 0.04, 0.01, 0.1, 0.07 ], inf)
  ).play(clock);
  Pbind(
    \instrument, \snare,
    \amp, Pseq(~hhAmps, 1),
    \pan, Prand([-0.8, -0.3, 0, 0.3, 0.8 ], inf),
    \dur, 0.5,
    \freq, 6645,
    \decay, 0.04,
    \rq, 0.04
  ).play(clock);
)

(
  SynthDef(\pummel, {
    arg outBus = 0, freq = 220, decay = 10, coef = 0, dur = 1, amp = 0.1;
    var env = Env.perc(0.01, 2).kr(2);
    var pluck = Pluck.ar(
      in: WhiteNoise.ar(amp),
      trig: env, // Impulse.kr(1),
      maxdelaytime: 60.reciprocal,
      delaytime: freq.reciprocal,
      decaytime: decay,
      coef: coef
    );
    Out.ar(outBus, Pan2.ar(pluck, 0));
  }).add;
)

(
  SynthDef(\pummelFilter, {
    arg inBus = 7, outBus = 0;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(84/60/2, 0.5pi).range(840, 1660),
      rq: 0.2
    );
    Out.ar(outBus, lpf);
  }).add;
)

Synth(\pummel, [ freq: 220, decay: 2, coef: 0.4 ]);
Scale.directory;
(
  var clock = TempoClock(84/60);
  var ost = [
    1, 0, 10, // Db c Bb
    1, 0, 10,
    0, -2, 8, // c Bb Ab
    0, -2, 8
  ];
  var decays = [
    3, 3, 17
  ];
  var coefs = [
    0.4, 0.4, 0.6 
  ];
  var amps = [
    0.2, 0.15, 0.4
  ];
  var durs = [ // Starts on beat 4
    0.5, 0.5, 3,
    0.5, 0.5, 11,
    0.5, 0.5, 3,
    0.5, 0.5, 11
  ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \pummel,
    \outBus, pBus,
    \group, g,
    \addAction, 0,
    \scale, Scale.chromatic,
    \degree, Pseq(ost, 2),
    \dur, Pseq(durs, 2),
    \amp, Pseq(amps, inf),
    \decay, Pseq(decays, inf), // Prand([2, 3, 2, 4], inf),
    \coef, Pseq(coefs, inf)
  ).play(clock);
  Synth.tail(g, \pummelFilter, [ inBus: pBus, outBus: 0 ]);
)

(
  SynthDef(\sawPuck, {
    arg outBus = 0, leadFreqDelta = 0, leadTimeDelta = 0, freq = 220, dur = 1, amp = 0.1, spunk = 1;
    var sustainTime = spunk * dur * 0.5;
    var releaseTime = spunk * dur * 0.3;
    //var env = Env.perc(0.01 + leadTimeDelta, dur * 0.8).kr(2);
    var env = EnvGen.kr(
      Env.new([0, 1, 0.8, 0], [0.01 + leadTimeDelta, sustainTime, releaseTime], -3),
      doneAction: Done.freeSelf
    );
    var saw = DPW4Saw.ar(
      freq: XLine.kr(freq + leadFreqDelta, freq, leadTimeDelta + 0.01),
      mul: amp
    ) * env;
    Out.ar(outBus, saw ! 2);
  }).add;
)
(
  SynthDef(\puckFilter, {
    arg inBus = 7, outBus = 0;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(84/60/3, 0.5pi).range(277, 830),
      rq: 0.2
    );
    Out.ar(outBus, lpf);
  }).add;
)
Synth(\sawPuck, [ freq: 261, leadFreqDelta: -54, leadTimeDelta: 0.04 ]);
Synth(\sawPuck, [ freq: 261, leadFreqDelta: 50, leadTimeDelta: 0.02, spunk: 0.5, dur: 0.5 ]);

(
  var clock = TempoClock(84/60);
  var freqs = [
    261, 261, 261, // C
    261, 261, 261, // C
    329, 329, 329, // E
    329, 329, 329, // E
    311, 311, 311, // Eb
    311, 311, 311, // Eb
    277, 277, 277,  // Db
    277, 277, 277,  // Db
    261, 261, 261, // C
    261, 261, 261, // C
    329, 329, 329, // E
    329, 329, 329, // E
    311, 311, 311, // Eb
    311, 311, 311, // Eb
    277, 277, 277,  // Db
    277, 277, 277  // Db
  ];
  var lfDeltas = [
    50, 0, -54, 50, 0, -54,
    63, 0, -68, 63, 0, -68,
    81, 0, -78, 81, 0, -78,
    52, 0, -70, 52, 0, -70
  ];
  var ltDeltas = [
    0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04,
    0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04,
    0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04,
    0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04, 0.02, 0, 0.04
  ];
  var durs = [ 0.5, 1.5, 2 ];
  var spunks = [ 0.5, 0.5 / 3, 0.5 ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \sawPuck,
    \outBus, pBus,
    \group, g,
    \addAction, 0,
    \freq, Pseq(freqs, 1),
    \leadFreqDelta, Pseq(lfDeltas ++ lfDeltas, 1),
    \leadTimeDelta, Pseq(ltDeltas ++ ltDeltas, 1),
    \dur, Pseq(durs, inf),
    \spunk, Pseq(spunks, inf)
  ).play(clock);
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0 ]);
)
