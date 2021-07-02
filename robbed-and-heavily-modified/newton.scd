(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  var newtonFilename = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/newton.wav";
  ~newtonBuf1 = Buffer.readChannel(s, newtonFilename, channels: [0]);
  ~newtonBuf2 = Buffer.readChannel(s, newtonFilename, channels: [0]);
)
(
  x = SynthDef(\help_Buffer, { arg out = 0, bufnum;
      Out.ar( out,
          PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
      )
  }).play(s,[\bufnum, ~newtonBuf1]);
)
x.free;

(
  // synthdefs: ///////////////////////////////////////////////////////////////////////
  SynthDef(\bgrain, { |out = 0, gate = 1, amp = 0, bufnum, gfreq=90, rate=0.5, dur = 0.8, trigmul = 1|
      var snd, pan, env, freqdev;
      pan = LFNoise1.kr(0.1).range(-1, 1) * LFNoise0.kr(gfreq).range(0.2,1);
      env = EnvGen.kr(
          Env([0, 1, 0], [1, 1], \sin, 1),
          gate,
          doneAction: Done.freeSelf);
    
    snd = GrainBuf.ar(
      numChannels:2,
      trigger:Impulse.kr(LFNoise1.kr(gfreq).range(0.8,1) * gfreq),
      // trigger: Impulse.kr(0.714285714 * trigmul * LFNoise1.kr(gfreq).range(0.97, 1.03)),
      // trigger: Impulse.kr(0.714285714 * trigmul),
      dur: dur  * LFNoise0.kr(gfreq).range(1,1.2),
      sndbuf: bufnum,
      rate: [rate * LFNoise1.kr.range(0.99, 1), rate * LFNoise1.kr.range(0.99, 1)],
      pos: LFNoise2.kr(0.05).range(0, 1) * LFNoise0.kr(gfreq).range(1, 1.02),
      // pos: LFTri.kr(0.25),
      interp: 2,
      pan: pan
    );

    snd = RLPF.ar(snd, freq: LFNoise2.kr(0.1).exprange(1500,4000));
    // snd = snd * LFTri.ar(0.073).range(0.1,1);
    snd = snd * env * amp;

    Out.ar(out, snd);

  }).add;
)

(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.3, dlyWet=0.2, feedback = 0.2 ;
    
    var snd = In.ar(inBus, 2);
    var verb = JPverb.ar(snd);
    var delay = Greyhole.ar(snd, feedback: feedback);
    
    snd = snd + (verb * revWet) + (delay * dlyWet) * 0.5;
    Out.ar(out, snd);
  }).add;
);


// -----------------------------------------------------------------
(
  // create a reverb/delay send bus
  ~reverBus = Bus.audio(s,2);

  // create processing verb/delay  plugin/synth 
  ~reverbDelay = Synth(\verbDelayFX, [\inBus, ~reverBus], addAction: \addAfter);

  // free them if necessary:
  // ~reverbDelay.free
  // ~reverBus.free
)

// -----------------------------------------------------------------

(
  // granulation 1
  ~newton1 = Synth(\bgrain,
    [\out, ~reverBus, \bufnum, ~newtonBuf1, \amp, 0.8, \dur, 0.4, \rate, 2, \trigmul, 2],
    addAction: \addToHead);
)

(
  // granulation 2
  ~newton2 = Synth(\bgrain,
    [\out, ~reverBus, \bufnum, ~newtonBuf2, \amp, 0.9, \dur: 0.05, \rate, 0.6666666666667, \trigmul, 0.66666666667],
    addAction: \addToHead);
)

// play with arguments:
~newton1.set(\amp, 0.8)
~newton1.set(\rate, 0.5)
~newton1.set(\amp, 1.2)
~newton1.set(\dur, 0.2)
~newton2.set(\rate, 1.5)
~newton2.set(\gfreq, 100)
~newton2.set(\gfreq, 120)

// fadeout
~netwon1.release(10)
~newton2.release(10)
