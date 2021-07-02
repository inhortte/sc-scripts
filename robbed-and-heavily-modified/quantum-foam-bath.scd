(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  var sketch3File = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/v5-sketch-3-part_oort-cloud.wav";
  var zubluronFile = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/zebluron.wav";
  var u2File = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/u2.wav";
  var tranquilFile = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/tranquil.wav";
  var xofFile = "/home/polaris/rummaging_round/sc-scripts/luka-liz/samples/xof.wav";

  ~sketch3Buf = Buffer.read(s, sketch3File);
  ~tranquilBuf = Buffer.read(s, tranquilFile);
  ~u2Buf = Buffer.read(s, u2File);
  ~xofBuf = Buffer.read(s, xofFile);
  ~zebluronBuf = Buffer.read(s, zubluronFile);
)

(
  SynthDef(\bungleBuf, { 
    arg outBus = 0, wobbleRate = 0.1, wobbleLow = 0.98, wobbleHigh = 1.02, bufnum;
    Out.ar(
      outBus,
      PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * SinOsc.kr(wobbleRate).range(wobbleLow, wobbleHigh))
    )
  }).add;
)
(
  SynthDef(\dustUm, {
    arg inBus = 7, outBus = 0, amp = 0.1;
    var env = EnvGen.ar(
      Env.circle([1, XLine.kr(0.01, 1, 40), 1, XLine.kr(0.02, 1, 40), 1, XLine.kr(0.03, 1, 40), 1], 
        [
          Line.kr(0.01, rrand(0.1, 0.3), 40), 
          Line.kr(0.1, rrand(0.2, 0.4), 40), 
          Line.kr(0.01, rrand(0.05, 0.2), 40), 
          Line.kr(0.001, rrand(0.01, 0.1), 40), 
          Line.kr(0.1, rrand(0.15, 0.2), 40), 
          Line.kr(0.01, rrand(0.02, 0.05), 40), 
          Line.kr(0.02, rrand(0.05, 0.07), 40)
        ], [-4, -1, 2, -5, 1, 0, -1])
    );
    /*
    var env = EnvGen.ar(
      Env.circle([1, 0.01, 1],
        [
          Line.kr(0.01, 0.3, 5), 
          Line.kr(0.1, 0.4, 5), 
          Line.kr(0.01, 0.2, 5)
        ], [-4, -1, 2, -5, 1])
    );
    */
    var in = In.ar(inBus, 2);
    var lpf = LPF.ar(
      in: in, 
      freq: 4000 * env,
      mul: 0.3 //XLine.kr(0.1, 0.3, 10)
    );
    Out.ar(outBus, lpf);
  }).add;
)

Synth(\dustUm);

(
  var pus = Bus.audio(s, 2);
  Synth(\dustUm, [ inBus: pus ]);
  Synth(\bungleBuf, [ bufnum: ~zebluronBuf, outBus: pus, wobbleRate: 0.04, wobbleLow: 0.97, wobbleHigh: 1.03 ]);
)
