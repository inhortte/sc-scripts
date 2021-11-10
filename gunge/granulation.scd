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

    ~warp = Buffer.read(s, "/flavigula/xian/warp.wav");
    ~riff1 = Buffer.read(s, "/flavigula/audacity/riff1.wav");
    // ~repriseIntroGuitarL = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/granulate-this-honeybunch.wav", channels: [0]);
    // ~repriseIntroGuitarR = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/granulate-this-honeybunch.wav", channels: [1]);
    // ~argonBassL = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/Bblyd7_Take60_Argon bass-1.3.wav", channels: [0]);
    // ~argonBassR = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/Bblyd7_Take60_Argon bass-1.3.wav", channels: [1]);
    ~transationGuitarL = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/transition-guitar.wav", channels: [0]);
    ~transationGuitarR = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/transition-guitar.wav", channels: [1]);
    ~guitar1L = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/guitar-4.wav", channels: [0]);
    ~guitar1R = Buffer.readChannel(s, "/flavigula/ardour/alfred/fehut-felu/export/guitar-4.wav", channels: [1]);
    ~lydianL = Buffer.readChannel(s, "/flavigula/ardour/haikus/first-winter-rain/export/lydian-fantasy.wav", channels: [0]);
    ~lydianR = Buffer.readChannel(s, "/flavigula/ardour/haikus/first-winter-rain/export/lydian-fantasy.wav", channels: [1]);
    ~grainy = {
      var sig, buf;
      buf = ~lydianL;
      sig = GrainBuf.ar(
        numChannels: 2,
        // trigger: Impulse.kr({ExpRand(60, 60.9)}!12),
        // trigger: Impulse.ar(128),
        trigger: Dust.kr(128),
        // dur: Line.kr(1/8, 1/64, BufDur.kr(buf)),
        dur: LFNoise1.kr(24).range(1/128, 1/32),
        // dur: LFNoise1.kr(128).range(0.005, 0.05),
        sndbuf: buf,
        rate: 1.0,
        /*
        pos: (Line.kr(0.0, BufSamples.kr(buf) - 1, BufDur.kr(buf), doneAction: 2) + 
          LFNoise1.kr(128).bipolar(SampleRate.ir * Line.kr(0.04, 0.3, BufDur.kr(buf)))) / 
          BufSamples.kr(buf),
        */
        pos: (Line.kr(0.0, BufSamples.kr(buf) - 1, 1 * BufDur.kr(buf), doneAction: 2) + 
          LFNoise1.kr(128).bipolar(SampleRate.ir * LFNoise1.ar(16).range(0.05, 0.16))) / 
          BufSamples.kr(buf),
        interp: 2,
        pan: LFNoise1.kr(64).range(-1, 1),
        // pan: 0,
        envbufnum: -1,
        maxGrains: 512,
        mul: 0.4
      );
      sig = Splay.ar(sig);
    };

    ~inputTest = {
      var sig;
      sig = SoundIn.ar([0, 1]);
      sig = sig + CombN.ar(sig, decaytime: 4, mul: 0.5);
    };
    ~grainInTest = {
      var sig;
      sig = SoundIn.ar([0, 1]);
      sig = GrainIn.ar(
        2,
        trigger: Dust.ar(64),
        dur: LFNoise1.kr(24).range(1/64, 1/32),
        in: sig,
        pan: LFNoise1.kr(52).range(-1, 1),
        mul: 0.5
      );
      sig = sig + CombN.ar(sig, delaytime: 0.88888888888888, decaytime: 2.66666666667, mul: 0.7);
    }

  });
)

{LFSaw.kr(10, 1)}.plot(0.2);
n = ~warp.play;
n.free;
~warp.free;
m = ~grainy.play;
m.stop;
m.free;
p = ~inputTest.play;
p.free;
q = ~grainInTest.play;
q.free;
