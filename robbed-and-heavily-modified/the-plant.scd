(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

( // load samples
  var plantFilename = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/corner-plant.wav";

  ~plantBuf1 = Buffer.readChannel(s, plantFilename, channels: [0]);
  ~plantBuf2 = Buffer.readChannel(s, plantFilename, channels: [0]);
)

(
  SynthDef(\bgrain2, { 
    arg out = 0, gate = 1, amp = 0, bufnum, rate=0.5, dur = 0.8;
    var snd, pan, env, freqdev;
    pan = LFNoise1.kr(0.1).range(-1, 1) * LFNoise0.kr(15).range(0.2,1);
    env = EnvGen.kr(
        Env([0, 1, 0], [1, 1], \sin, 1),
        gate,
        doneAction: Done.freeSelf);
    
    snd = GrainBuf.ar(
      numChannels:2,
      trigger: Impulse.kr(0.333333333333 + LFNoise1.kr(240).range(-0.015, 0.015)),
      dur: dur  * LFNoise0.kr(30).range(0.1, 0.5),
      sndbuf: bufnum,
      rate: [rate * LFNoise1.kr.range(0.99, 1), rate * LFNoise1.kr.range(0.99, 1)],
      pos: LFNoise2.kr(12).range(0, 1), // * LFNoise0.kr(45).range(1, 1.02),
      interp: 2,
      pan: pan
    );

    // snd = RLPF.ar(snd, freq: LFNoise2.kr(0.1).exprange(1500,4000));
    // snd = snd * LFTri.ar(0.073).range(0.1,1);
    snd = snd * env * amp;

    Out.ar(out, snd);

  }).add;
)
(
  SynthDef(\bgrain3, { 
    arg out = 0, gate = 1, amp = 0, bufnum, rate=0.5, dur = 0.8;
    var snd, pan, env, freqdev;
    pan = LFNoise1.kr(0.1).range(-1, 1) * LFNoise0.kr(15).range(0.2,1);
    env = EnvGen.kr(
        Env([0, 1, 0], [1, 1], \sin, 1),
        gate,
        doneAction: Done.freeSelf);
    
    snd = GrainBuf.ar(
      numChannels:2,
      trigger: Impulse.kr(1.333333333333 + LFNoise1.kr(60).range(-0.06, 0.06)),
      dur: dur  * LFNoise0.kr(30).range(0.5, 1.5),
      sndbuf: bufnum,
      rate: [rate * LFNoise1.kr.range(0.99, 1), rate * LFNoise1.kr.range(0.99, 1)],
      pos: LFNoise2.kr(0.05).range(0, 1) * LFNoise0.kr(45).range(1, 1.02),
      interp: 2,
      pan: pan
    );

    // snd = RLPF.ar(snd, freq: LFNoise2.kr(0.1).exprange(1500,4000));
    // snd = snd * LFTri.ar(0.073).range(0.1,1);
    snd = snd * env * amp;

    Out.ar(out, snd);

  }).add;
)

(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.8, dlyWet=0.8, feedback = 0.5 ;
    
    var snd = In.ar(inBus, 2);
    // var verb = JPverb.ar(snd);
    var delay = Greyhole.ar(snd, feedback: feedback);
    
    snd = snd + (delay * dlyWet) * 0.5;
    Out.ar(out, snd);
  }).add;
);

(
  ~reverBus = Bus.audio(s,2);
  ~reverbDelay = Synth(\verbDelayFX, [\inBus, ~reverBus, \dlyWet, 0.1], addAction: \addAfter);
)

(
  ~plant1 = Synth(\bgrain3,
    [\out, ~reverBus, \bufnum, ~plantBuf1, \rate, 0.5, \amp, 3, \dur, 0.2],
    addAction: \addToHead
  );
)
(
  ~plant2 = Synth(\bgrain3,
    [\out, ~reverBus, \bufnum, ~plantBuf2, \rate, 1, \amp, 2],
    addAction: \addToHead
  );
)

~plant1.release(20);
~plant2.release(10);
