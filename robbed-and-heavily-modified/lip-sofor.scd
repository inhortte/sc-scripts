(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

(
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/luka-liz/lip-sofor-samples");

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
  var sr = ~getSmp.("gorge-1").sampleRate;
  var nf = ~getSmp.("gorge-1").numFrames;
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
  ~belch = { |regexp, outBus, parts, gap, times|
    var smp = ~getSmp.(regexp);
    var sr = smp.sampleRate;
    var startPoses = Array.fill(parts, {
      arg i;
      ((smp.numFrames / parts) * i).trunc;
    });
    Routine({
      times.do({
        startPoses.do({
          arg startPos;
          var dur = rrand(0.002, 0.06);
          Synth(\ggrain,
            [
              \outBus, outBus,
              \bufnum, smp,
              \rate, [1.0, 1.6666, 0.5, 2.0, 0.25, 3.0, 4.0].wchoose([0.5, 0.05, 0.2, 0.1, 0.05, 0.025, 0.075]),
              \startPos, startPos,
              \amp, rrand(0.7, 1.2),
              \pan, 1.0.rand2,
              \dur, dur
            ]
          );
          (gap - dur).wait;
        });
      });
    }).play
  };
)


(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.4, dlyWet=0.3, feedback = 0.5 ;
    
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
  ~belch.("complete", ~reverBus, rrand(100, 400), [0.125].choose, 4);
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

