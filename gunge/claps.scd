(
  s.plotTree;
  s.meter;
  s.scope;
)
(
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;

    ~clap1 = Buffer.readChannel(s, "/flavigula/sample-library/claps/1.wav", channels: [0]);
    ~clap2 = Buffer.readChannel(s, "/flavigula/sample-library/claps/2.wav", channels: [1]);
    ~clap3 = Buffer.readChannel(s, "/flavigula/sample-library/claps/3.wav", channels: [0]);
    ~clap4 = Buffer.readChannel(s, "/flavigula/sample-library/claps/4.wav", channels: [1]);
    ~goatToy = Buffer.read(s, "/flavigula/sample-library/toy-goat/goat-toy.wav");

    ~grainy = {
      var sig;
      sig = GrainBuf.ar(
        numChannels: 1,
        trigger: Impulse.kr({ExpRand(60, 60.9)}!12),
        dur: LFNoise1.kr(0.2!12).range(1/2048, 1/16),
        // dur: SinOsc.kr(23).range(1/256, 1/128),
        sndbuf: ~goatToy,
        rate: 1,
        pos: SinOsc.kr(23).range(0.1, 0.9),
        interp: 2,
        // pan: SinOsc.kr(221).range(-1, 1),
        pan: 0,
        envbufnum: -1,
        maxGrains: 512,
        mul: 0.3
      );
      sig = Splay.ar(sig);
    };

    SynthDef(\clapper, {
      var jen, taf, tir, kar, env, scale1, scale2, scale3, scale4, sum;
      env = Env.perc(0.01, 2.0, \amp.ir(0.2), curve: -1).kr(2);
      jen = 0;
      scale1 = BufRateScale.kr(~clap1) * \jenRate.ir(1.0);
      scale2 = BufRateScale.kr(~clap2) * \tafRate.ir(1.0);
      scale3 = BufRateScale.kr(~clap3) * \tirRate.ir(1.0);
      scale4 = BufRateScale.kr(~clap4) * \karRate.ir(1.0);
      jen = PlayBuf.ar(
        numChannels: 1, 
        bufnum: ~clap1, 
        rate: scale1, 
        trigger: 1.0, 
        startPos: \jenPos.ir(0.0)
      );
      taf = PlayBuf.ar(
        numChannels: 1, 
        bufnum: ~clap2, 
        rate: scale2, 
        trigger: 1.0, 
        startPos: \tarPos.ir(0.0)
      );
      tir = PlayBuf.ar(
        numChannels: 1, 
        bufnum: ~clap3, 
        rate: scale3, 
        trigger: 1.0, 
        startPos: \tirPos.ir(0.0)
      );
      kar = PlayBuf.ar(
        numChannels: 1, 
        bufnum: ~clap4, 
        rate: scale4, 
        trigger: 1.0, 
        startPos: \karPos.ir(0.0)
      );
      jen = Pan2.ar(jen, pos: {Rand(-1.0, 1.0)}, level: 0.4);
      taf = Pan2.ar(taf, pos: {Rand(-1.0, 1.0)}, level: 0.4);
      tir = Pan2.ar(tir, pos: {Rand(-1.0, 1.0)}, level: 0.4);
      kar = Pan2.ar(kar, pos: {Rand(-1.0, 1.0)}, level: 0.4);
      sum = jen + taf + tir + kar;
      Out.ar(0, sum); 
    }).add;

    ~clapTrack = Pbind(
      \instrument, \clapper,
      \jenPos, rrand(0.0, 0.2),
      \jenRate, rrand(1.0, 1.5),
      \tarPos, rrand(0.0, 0.2),
      \tarRate, rrand(1.0, 2.0),
      \tirPos, rrand(0.0, 0.2),
      \tirRate, rrand(0.5, 2.5),
      \karPos, rrand(0.0, 0.2),
      \karRate, rrand(0.7, 1.5),
      \dur, 4
    );
  });
)

Synth(\clapper);
p = ~clapTrack.play(TempoClock(84/60));
p.stop;

~clap1.numFrames
BufRateScale.kr(~clap1) * 1.5

n = ~grainy.play
n.free;
