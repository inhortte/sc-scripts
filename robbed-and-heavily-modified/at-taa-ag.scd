(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

(
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/robbed-and-heavily-modified/serbian-princess-samples");

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
  var sr = ~getSmp.("bojana").sampleRate;
  var nf = ~getSmp.("bojana").numFrames;
  (nf / sr).postln
)

(
  SynthDef(\ggrain, {
    arg outBus, bufnum = 0, pan = 0.0, startPos = 0.0, attack = 0.01, amp = 0.1, dur = 0.2, rate = 1.0;
    //var grain = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, BufFrames.kr(bufnum) * startPos, 0) *
    var grain = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, 1, startPos, 0) *
      (
        EnvGen.kr(
          Env.perc(attack, dur),
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
          var dur = rrand(0.3125, 0.625);
          var attack = 0.1; // rrand(0.05, 0.1);
          Synth(\ggrain,
            [
              \outBus, outBus,
              \bufnum, smp,
              \rate, [0.5].wchoose([0.6, 0.2, 0.2]),
              \startPos, startPos,
              \amp, [0.5, 0.7, 0.8, 0.3, 0.0].wchoose([0.2, 0.1, 0.05, 0.2, 0.45]) * 2,
              \pan, 1.0.rand2,
              \attack, attack,
              \dur, dur
            ]
          );
          gap.wait;
          // (gap - dur).wait;
        });
      });
    }).play
  };
)


(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.5, dlyWet=0.5, feedback = 0.2 ;
    
    var snd = In.ar(inBus, 2);
    var verb = JPverb.ar(snd);
    var delay = Greyhole.ar(
      in: snd, 
      delayTime: 1.071429,
      feedback: feedback
    );
    
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
  ~belch.("krzys", 0, rrand(3, 5), [0.625].choose, 10);
  // ~belch.("Db", ~reverBus, rrand(3, 6), [0.714286, 0.357143, 0.5357142].choose, 12);
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


