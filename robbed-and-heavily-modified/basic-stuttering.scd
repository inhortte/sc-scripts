(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
s.meter;
s.scope;

( // load samples
  var waterFilename = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/running-water.wav";

  ~runningWaterBuf = Buffer.readChannel(s, waterFilename, channels: [0]);
)
(
  var smpPath = PathName("/home/polaris/rummaging_round/sc-scripts/luka-liz/tetyk-samples");
  ~sampleNames = [
    "able-bodied", "alpha-centauri", "apple-pie", "broccoli", "cranberry", "darkness", "desk", "emily-warner", "i-wish-you-were", "nobody-lives-forever", "porcelain-pig", "the-way-we-were"
  ];

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
  }).play(s,[\bufnum, ~runningWaterBuf]);
)
x.free;

~runningWaterBuf.sampleRate.postln;

(
  SynthDef(\oneGrain, {
    arg outBus, bufnum = 0, pan = 0.0, startPos = 0.0, dur = 0.2, rate = 1.0, amp = 0.1;
    var grain = PlayBuf.ar(
      numChannels: 1,
      bufnum: bufnum,
      rate: BufRateScale.kr(bufnum) * rate,
      trigger: 1,
      startPos: startPos
    ) * EnvGen.kr(
      Env.perc(0.01, dur),
      doneAction: Done.freeSelf
    ) * amp;
    Out.ar(outBus, Pan2.ar(grain, pan));
  }).add;
)

(
  SynthDef(\mangleFX, {
    arg inBus, outBus, filtFreq = 220, cycDur = 1.0, ghFeedback = 0.4, amp = 1;
    var lpf = RLPFD.ar(
      in: In.ar(inBus, 2),
      ffreq: SinOsc.kr(cycDur).range(filtFreq / 2, filtFreq * 2),
      res: 0.4,
      dist: SinOsc.kr(cycDur * rrand(0.8, 1.2)).range(0.3, 0.6)
    ) * amp;
    var gh = Greyhole.ar(
      in: lpf,
      delayTime: SinOsc.kr(0.2).range(0.8, 1.2),
      damp: 0.3,
      diff: 0.4,
      feedback: ghFeedback,
      modDepth: 0,
      modFreq: 0
    );
    Out.ar(outBus, gh);
  }).add;
)

(
  ~fxBus = Bus.audio(s, 2);
  ~mangleFx = Synth(\mangleFX, [inBus: ~fxBus, filtFreq: 512, amp: 3, cycDur: 0.1], addAction: \addAfter);
)

(
  var grainLength = ~runningWaterBuf.numFrames / 24;
  var startPoses = Array.fill(24, { |i| i * grainLength });
  Pbind(
    \instrument, \oneGrain,
    \outBus, ~fxBus,
    \bufnum, ~runningWaterBuf,
    \pan, Prand(Array.fill(11, { |i| 1 - (i / 5) }), inf),
    \startPos, Pseq(startPoses, inf),
    \dur, 0.25,
    \amp, 8,
    \rate, 1.0
  ).play(TempoClock(64/60));
)

(
  Routine({
    ~sampleNames.do({
      arg name;
      var sample = ~getSmp.(name);
      var boof = rrand(5, 12);
      var gl = sample.numFrames / boof;
      var startPoses = Array.fill(boof, { |i| i * gl });
      startPoses.postln;
      Pbind(
        \instrument, \oneGrain,
        \outBus, 0,
        \bufnum, sample,
        \pan, Prand(Array.fill(11, { |i| 1 - (i / 5) }), inf),
        \startPos, Pseq(startPoses, 1),
        \dur, 0.25,
        \amp, 1,
        \rate, 1.0
      ).play(TempoClock(64/60));
      (sample.numFrames  / sample.sampleRate + (60 / 64 * 3)).wait;
    });
  }).play;
)
