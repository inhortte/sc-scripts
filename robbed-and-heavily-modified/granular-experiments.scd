(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

(
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/luka-liz/tyk-jena-tin-nulu-samples");
  ~bufNames = smpPath.files.collect { |file| 
    file.fileName
  };
)

(
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/luka-liz/tyk-jena-tin-nulu-samples");

  ~smpBuffers.do(_.free); 
  ~smpBuffers = Dictionary();
  smpPath.filesDo({ 
    arg smpfile, i;
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
  x = SynthDef(\help_Buffer, { arg out = 0, bufnum;
      Out.ar( out,
          PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
      )
  }).play(s,[\bufnum, ~getSmp.("fuzz-4")]);
)
x.free;

(
  var sr = ~getSmp.("belch-6").sampleRate;
  var nf = ~getSmp.("belch-6").numFrames;
  (nf / sr).postln
)

(
  SynthDef(\ggrain, {
    arg outBus, bufnum = 0, pan = 0.0, startPos = 0.0, amp = 0.1, dur = 0.2, rate = 1.0;
    //var grain = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, BufFrames.kr(bufnum) * startPos, 0) *
    var grain = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, 1, startPos, 0) *
      (
        EnvGen.kr(
          Env.perc(0.01, dur),
          doneAction: Done.freeSelf
        ) - 0.001
      ) * amp;
    Out.ar(outBus, Pan2.ar(grain, pan))
  }).add; 
)

Array.fill(5, { |i| ((117953 / 5) * i).trunc; }).keep(2)

(
  ~belch = { |regexp, outBus, parts, gap|
    var smp = ~getSmp.(regexp);
    var sr = smp.sampleRate;
    var startPoses = Array.fill(parts, {
      arg i;
      ((smp.numFrames / parts) * i).trunc;
    });
    Routine({
      startPoses.do({
        arg startPos;
        Synth(\ggrain,
          [
            \outBus, outBus,
            \bufnum, smp,
            \rate, [1.0, 0.5].wchoose([0.8, 0.2]),
            \startPos, startPos,
            \amp, rrand(0.4, 0.7),
            \pan, 1.0.rand2,
            \dur, rrand(0.02, 0.16)
          ]
        );
        rrand(0.1, gap).wait;
      });
    }).play
  };
)

(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.4, dlyWet=0.2, feedback = 0.7 ;
    
    var snd = In.ar(inBus, 2);
    var verb = JPverb.ar(snd);
    var delay = Greyhole.ar(snd, feedback: feedback);
    
    snd = snd + (verb * revWet) + (delay * dlyWet) * 0.5;
    Out.ar(out, snd);
  }).add;
);


// -----------------------------------------------------------------
(
  ~reverBus = Bus.audio(s,2);
  ~reverbDelay = Synth(\verbDelayFX, [\inBus, ~reverBus], addAction: \addAfter);
)

(
  var bufs = ~bufNames.scramble.keep(6);
  bufs.postln;
  Routine({
    bufs.do({
      arg bufName;
      // var gap = rrand(1.5, 3);
      // var gap = rrand(0.8, 1.5);
      var gap = rrand(0.2, 1);
      //var gap = rrand(0.05, 0.5);
      // var parts = rrand(3, 6);
      var parts = rrand(8, 12);
      ~belch.(bufName, ~reverBus, parts, gap);
      (gap * parts / 3).wait;
    });
  }).play;
)

~belch.("overdrive-3", rrand(10, 30), rrand(0.1, 1))

(
  Routine({
    80.do {
      arg i; 
      var timestart, timeend;
      var lastval = rrand(0.01, 1);
      var boing = 40.0 - i;
      var prop = if(boing < 0.0, { 64.0 + boing }, { 40.0 - boing + 16.0 });
      prop = (prop / 80.0);
      timestart = prop * 0.8;
      timeend = prop * (0.8 + (0.1 * lastval));
      Synth(\ggrain,
        [
          \outBus, ~rlpf3Bus,
          \bufnum, ~bufThree.bufnum, 
          \startPos,rrand(timestart, timeend),
          \amp, exprand(0.5,0.9), 
          \pan, 1.0.rand2,
          \dur, 0.1 + rrand(lastval * 0.2, lastval)
        ]
      );  
      rrand((prop * 0.1).max(0.2), prop * 0.5).wait;
    }
  }).play
)

(
  {
    var singrain1, singrain2, sfgrain;
    singrain1=SinOsc.ar(440,0,XLine.kr(1.0, 0.0001, 0.05));
    singrain2=FSinOsc.ar(800, 0.0, Line.kr(1.0,0,0.05).squared);
    sfgrain=(PlayBuf.ar(1,b.bufnum, BufRateScale.kr(b.bufnum)))*EnvGen.kr(Env([0,1,1,0],[0.01,0.01,0.01], -4));
    [singrain1,singrain2,sfgrain]
  }.plot(0.1,s)
)
{ XLine.kr(1.001,0.001,0.1,doneAction:2) }.plot
(
  SynthDef(\sinegrain, {
    arg pan, freq, amp; var grain; 
    grain= SinOsc.ar(freq, 0, amp)*(XLine.kr(1.001,0.001,0.1,doneAction:2)-0.001);
    Out.ar(0,Pan2.ar(grain, pan));
  }).add; 
)
Synth(\sinegrain,[\freq,rrand(100,10000),\amp, exprand(0.05,0.1), \pan, 1.0.rand2]);
(
  {
    100.do{
      arg i; 
      Synth(\sinegrain,[\freq,rrand(100,10000),\amp, exprand(0.05,0.1), \pan, 1.0.rand2]);  
      0.01.wait;
    }; 
  }.fork
)



(
  384.do {
    arg i;
    var boing = 192 - i;
    var timeprop = if(boing < 0, { 256 + boing }, { 192 - boing  + 64});
    // i.postln;
    timeprop.postln;
  }
)

(
  Routine({
    loop({
      384.do{
        arg i; 
        var boing = 192.0 - i;
        var timeprop = if(boing < 0.0, { 256.0 + boing }, { 192.0 - boing + 64.0 });
        timeprop = (timeprop / 255.0) ** 2;
        Synth(\ggrain,
          [
            \bufnum, b.bufnum, 
            \startPos,rrand(0.0, timeprop),
            \amp, exprand(0.005,0.1), 
            \pan, 1.0.rand2
          ]
        );  
        rrand((timeprop * 0.2).max(0.02), timeprop * 0.8).wait;
      }; 
    })
  }).play
)

(
  SynthDef(\bgrain1, { 
    arg out = 0, gate = 1, amp = 0, bufnum, rate=0.5, dur = 0.8;
    var snd, pan, env, freqdev;
    pan = LFNoise1.kr(0.1).range(-1, 1) * LFNoise0.kr(15).range(0.2,1);
    env = EnvGen.kr(
        Env([0, 1, 0], [1, 1], \sin, 1),
        gate,
        doneAction: Done.freeSelf);
    
    snd = GrainBuf.ar(
      numChannels:2,
      trigger: Impulse.kr(LFNoise1.kr(90).range(0.8, 1) * 90),
      dur: dur  * LFNoise0.kr(90).range(1, 1.5),
      sndbuf: bufnum,
      rate: [rate * LFNoise1.kr.range(0.99, 1), rate * LFNoise1.kr.range(0.99, 1)],
      pos: LFNoise2.kr(0.05).range(0, 1) * LFNoise0.kr(90).range(1, 1.02),
      interp: 2,
      pan: pan
    );

    snd = RLPF.ar(snd, freq: LFNoise2.kr(0.1).exprange(400, 4000));
    // snd = snd * LFTri.ar(0.073).range(0.1,1);
    snd = snd * env * amp;

    Out.ar(out, snd);

  }).add;
)
(
  ~toothJeniz = Synth(\bgrain2,
    [\out, 0, \bufnum, c.bufnum, \rate, 0.5, \amp, 0.5, \dur, 0.1],
    addAction: \addToHead
  );
)
~toothJeniz.set(\amp, 0.5);
~toothJeniz.set(\rate, 2);
~toothJeniz.release(10);



(
  var w, slid, lastval;
  lastval=0.0;
  w = Window("My Window", Rect(100,500,200,200)); 
  // A 200 by 200 window appears at screen co-ordinates (100, 500)
  slid = Slider(w,Rect(10,10,150,40)); //a basic slider object 
  slid.action_({
    lastval= slid.value;
  }); //this is the callback- the function is called whenever you move the slider

  {
    inf.do{
      arg i; 
      var prop, timestart, timeend;
      prop = (i % 300) / 300;
      timestart = prop * 0.8;
      timeend = prop * (0.8 + (0.1 * lastval));
      Synth(\ggrain,
        [
          \bufnum, ~bufOne.bufnum, 
          \startPos, rrand(timestart, timeend),
          \amp, exprand(0.005,0.1), 
          \pan, lastval.rand2, 
          \dur, 0.1+(lastval*0.5)
        ]
      );  
      //max in this to avoid ever going near 0.0 wait time, which would crash the computer!
      (((lastval*0.2)+0.01).max(0.01)).wait
    }; 
  }.fork;
  w.front;
)

(
  SynthDef(\rlpfEtiz, {
    arg inBus, outBus = 0, nol = 1.0, freq = 440, amp = 0.1;
    
    var snd = In.ar(inBus, 2);
    var env = EnvGen.kr(
      Env.circle([0.25, 1, 0.25], [nol * 0.33333333, nol * 0.33333333, nol * 0.333333333333])
    );
    var rlpf = RLPF.ar(
      in: snd,
      freq: freq * env * 2,
      rq: 0.2
    ) * amp;
    Out.ar(outBus, rlpf);
  }).add;
)

(
  SynthDef(\bpfEtiz, {
    arg inBus, outBus = 0, nol = 1.0, freq = 440, amp = 0.1;
    
    var snd = In.ar(inBus, 2);
    var env = EnvGen.kr(
      Env.circle([0.25, 1, 0.25], [nol * 0.33333333, nol * 0.33333333, nol * 0.333333333333])
    );
    var rlpf = BPF.ar(
      in: snd,
      freq: freq * env * 2,
      rq: 0.2
    ) * amp;
    Out.ar(outBus, rlpf);
  }).add;
)

(
  ~rlpfBus = Bus.audio(s,2);
  ~rlpf = Synth(\rlpfEtiz, [\inBus, ~rlpfBus, \amp, 0.3, \freq, 220, \nol, 8], addAction: \addAfter);
)
(
  ~rlpf2Bus = Bus.audio(s, 2);
  ~rlpf2 = Synth(\bpfEtiz, [\inBus, ~rlpf2Bus, \amp, 0.3, \freq, 440, \nol, 4], addAction: \addAfter);
)
(
  ~rlpf3Bus = Bus.audio(s, 2);
  ~rlpf3 = Synth(\bpfEtiz, [\inBus, ~rlpf3Bus, \amp, 0.3, \freq, 880, \nol, 16], addAction: \addAfter);
)
(
  ~rlpf4Bus = Bus.audio(s, 2);
  ~rlpf4 = Synth(\bpfEtiz, [\inBus, ~rlpf4Bus, \amp, 0.3, \freq, 1760, \nol, 16], addAction: \addAfter);
)

~bufOne = Buffer.readChannel(s, "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/cumple-jeniz.wav", channels: [0]);
~bufTwo = Buffer.readChannel(s, "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/cumple-tafiz.wav", channels: [0]);
~bufThree = Buffer.readChannel(s, "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/cumple-tiriz.wav", channels: [0]);
~bufFour = Buffer.readChannel(s, "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/cumple-kariz.wav", channels: [0]);
Synth(\ggrain,[\outBus, ~rlpfBus, \bufnum, ~bufOne.bufnum, \startPos, rrand(0.0,1.0), \amp, exprand(0.4,0.9), \pan, 1.0.rand2]);  

(
  Routine({
    384.do {
      arg i; 
      var timestart, timeend;
      var lastval = rrand(0.01, 1);
      var boing = 192.0 - i;
      var prop = if(boing < 0.0, { 256.0 + boing }, { 192.0 - boing + 64.0 });
      prop = (prop / 255.0);
      timestart = prop * 0.8;
      timeend = prop * (0.8 + (0.5 * lastval));
      Synth(\ggrain,
        [
          \outBus, ~rlpfBus,
          \bufnum, ~bufOne.bufnum, 
          \startPos,rrand(timestart, timeend),
          \amp, exprand(0.5,0.9), 
          \pan, 1.0.rand2,
          \dur, 0.1 + (lastval * 0.5)
        ]
      );  
      rrand((prop * 0.01).max(0.05), prop * 0.2).wait;
    }
  }).play
)

(
  Routine({
    1536.do {
      arg i; 
      var timestart, timeend;
      var lastval = rrand(0.01, 1);
      var boing = 512.0 - i;
      var prop = if(boing < 0.0, { 1024.0 + boing }, { 512.0 - boing + 256.0 });
      prop = (prop / 1023.0);
      timestart = prop * 0.8;
      timeend = prop * (0.8 + (0.5 * lastval));
      Synth(\ggrain,
        [
          \outBus, ~rlpf2Bus,
          \bufnum, ~bufTwo.bufnum, 
          \startPos,rrand(timestart, timeend),
          \amp, exprand(0.5,0.9), 
          \pan, 1.0.rand2,
          \dur, 0.05 + (lastval * 0.1)
        ]
      );  
      rrand((prop * 0.01).max(0.05), prop * 0.2).wait;
    }
  }).play
)

(
  Routine({
    80.do {
      arg i; 
      var timestart, timeend;
      var lastval = rrand(0.01, 1);
      var boing = 40.0 - i;
      var prop = if(boing < 0.0, { 64.0 + boing }, { 40.0 - boing + 16.0 });
      prop = (prop / 80.0);
      timestart = prop * 0.8;
      timeend = prop * (0.8 + (0.1 * lastval));
      Synth(\ggrain,
        [
          \outBus, ~rlpf3Bus,
          \bufnum, ~bufThree.bufnum, 
          \startPos,rrand(timestart, timeend),
          \amp, exprand(0.5,0.9), 
          \pan, 1.0.rand2,
          \dur, 0.1 + rrand(lastval * 0.2, lastval)
        ]
      );  
      rrand((prop * 0.1).max(0.2), prop * 0.5).wait;
    }
  }).play
)

(
  Routine({
    loop({
      384.do {
        arg i; 
        var timestart, timeend;
        var lastval = rrand(0.01, 1);
        var boing = 192.0 - i;
        var prop = if(boing < 0.0, { 256.0 + boing }, { 192.0 - boing + 64.0 });
        prop = (prop / 255.0);
        timestart = prop * 0.8;
        timeend = prop * (0.8 + (0.1 * lastval));
        Synth(\ggrain,
          [
            \outBus, ~rlpf4Bus,
            \bufnum, ~bufFour.bufnum, 
            \startPos,rrand(timestart, timeend),
            \amp, exprand(0.5,0.9), 
            \pan, 1.0.rand2,
            \dur, 0.05 + rrand(lastval * 0.1, lastval * 0.4)
          ]
        );  
        rrand((prop * 0.02).max(0.05), prop * 0.2).wait;
      }
    })
  }).play
)
