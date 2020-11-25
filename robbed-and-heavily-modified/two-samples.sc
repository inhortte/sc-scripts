// #looptober 2020 02
// adapted from Luka Prinčič <luka@lukaprincic.si>
// by Bobbus Mustelidus <flavigula@protonmail.com>
// free to use, remix, etc under conditions of  peer production licence:
// https://wiki.p2pfoundation.net/Peer_Production_License

(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

( // load samples
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/luka-liz/samples");

  // free all buffers to restart buffer count
  ~smpBuffers.do(_.free); 

  // create Dictionary
  ~smpBuffers = Dictionary();

  // load samples 
  "\n--- load samples: ...".postln;

  // iterate over each file in the folder
    smpPath.filesDo({ |smpfile,i|
      // tell me what you are loading:
      postln("   " + i + smpfile.fileName );

      // add a sample into a buffer, store object to Dictionary
      ~smpBuffers.add(smpfile.fileName -> Buffer.readChannel(s,
          smpfile.fullPath, channels:[0]));
    });

  // function to partially match filename for buffers
  ~getSmp = { |regexp|
    ~smpBuffers.detect { |buf|
      regexp.matchRegexp(buf.path)
    }
  };
)

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
    arg inBus, out=0, revWet=0.8, dlyWet=0.8, feedback = 0.5 ;
    
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
  ~bbChime1 = Synth(\bgrain,
    [\out, ~reverBus, \bufnum, ~getSmp.("Bb-chime-1"), \amp, 0.8, \dur, 0.4, \rate, 2, \trigmul, 2],
    addAction: \addToHead);
)

(
  // granulation 2
  ~bbChime2 = Synth(\bgrain,
    [\out, ~reverBus, \bufnum, ~getSmp.("Bb-chime-2"), \amp, 0.9, \dur: 0.6, \rate, 0.6666666666667, \trigmul, 0.66666666667],
    addAction: \addToHead);
)

(
  // granulation 3
  ~bbChime3 = Synth(\bgrain,
    [\out, ~reverBus, \busnum, ~getSmp.("Bb-chime-3"), \amp, 1, \dur: 0.3, \rate, 0.5, \trigmul, 2.666666666666667],
    addAction: \addToHead);
)

(
  ~comboChime = Synth(\bgrain, 
    [\out, ~reverBus, \busnum, ~getSmp.("combo-chime"), \amp, 1, \dur, 0.5, \rate, 1.33333333],
    addAction: \addToHead
  );
)

(
  ~gChime = Synth(\bgrain, 
    [\out, ~reverBus, \busnum, ~getSmp.("g-chime"), \amp, 0.4, \dur, 0.8, \rate, 0.5],
    addAction: \addToHead
  );
)

// play with arguments:
~bbChime1.set(\amp, 0.8)
~bbChime1.set(\rate, 0.5)
~bbChime2.set(\amp, 0.7)
~bbChime2.set(\dur, 0.1)
~bbChime2.set(\rate, 0.5)
~bbChime2.set(\gfreq, 100)
~bbChime3.set(\gfreq, 120)
~bbChime3.set(\dur, 0.3)
~bbChime3.set(\amp, 1)
~bbChime3.set(\rate, 0.5)
~gChime.set(\rate, 0.3333333333333)
~gChime.set(\rate, 0.25)
~gChime.set(\rate, 0.16666666666666667)
~gChime.set(\rate, 0.125)

// fadeout
~bbChime1.release(10)
~bbChime2.release(10)
~bbChime3.release(10)
~gChime.release(10)
~comboChime.release(10)
