(
  var durs, amps, pans;
  ServerTree.removeAll;
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  ~reverbBus = Bus.audio(s, 2);
  ~tziharaNodes = {
    s.bind({
      ~fxGroup = Group.new;
      ~reverb = Synth(\reverberation, [ \inBus, ~reverbBus, \mix, 0.99, \room, 0.99 ], ~fxGroup);
    });
  };
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;

    SynthDef(\reverberation, {
      var sig, wet;
      sig = In.ar(\inBus.ir(0), 2);
      sig = FreeVerb2.ar(sig[0], sig[1], \mix.ir(0.99), \room.ir(0.99), \damp.ir(0.2));
      sig = LPF.ar(sig, \cutoff.ir(1174));  // D6
      Out.ar(\outBus.ir(0), sig);
    }).add;
    

    SynthDef(\bufPlay, {
      var sig, env;
      sig = PlayBuf.ar(2, \buf.ir(0), BufRateScale.kr(\buf.ir(0)) * \rate.ir(1), 1, \start.ir(0), \loop.ir(0));
      env = Env(
        [0, 1, 1, 0],
        [\attack.ir(0.05), \sustain.ir(0.95), \release.ir(0.05)],
        \lin
      ).ar(2);
      sig = sig * env * \amp.ir(0.2);
      Out.ar(\outBus.ir(0), sig);
      Out.ar(\outFxBus.ir(0), sig * \send.ir(-20).dbamp);
    }).add;

    SynthDef(\bufSlide, {
      var sig, env, line;
      line = Line.kr(\from.ir(1), \to.ir(0.5), \slideDur.ir(1));
      sig = PlayBuf.ar(2, \buf.ir(0), BufRateScale.kr(\buf.ir(0)) * line, 1, \start.ir(0), \loop.ir(0));
      env = Env(
        [0, 1, 1, 0],
        [\attack.ir(0.05), \sustain.ir(0.95), \release.ir(0.05)],
        \lin
      ).ar(2);
      sig = sig * env * \amp.ir(0.2);
      Out.ar(\outBus.ir(0), sig);
      Out.ar(\outFxBus.ir(0), sig * \send.ir(-20).dbamp);
    }).add;

    SynthDef.new(\hats, {
      arg outBus, atk=0.005, rel=0.3, pan=0, mul=0.2, amp=0.2;
      var sig, range=1, ampEnv, noiseEnv, noiseDurRatio=0.05;

      ampEnv = EnvGen.kr(Env.perc(atk, rel), doneAction:2);
      noiseEnv = EnvGen.kr(Env.perc(atk, rel*noiseDurRatio));

      sig = [345, 432, 543, 654, 765].collect({|freq|
        Pulse.ar(freq, SinOsc.kr(freq / 14));
      });
      sig = HPF.ar(sig, 11000) * 4;
      sig = LPF.ar(
        in: sig,
        freq: LFNoise0.kr(2).range(4500, 9000)
      );
      sig = Mix.new(sig) * ampEnv;
      sig = sig + (PinkNoise.ar * noiseEnv) * amp;

      Out.ar(outBus, Pan2.ar(sig, pan, mul));
    }).add;

    s.sync;
    durs = Array.fill(128, { [0.0625!8, 0.125!4, 0.25!2].wchoose([0.05, 0.35, 0.6]); }).flatten;
    amps = Array.fill(128, { [0.2, 0.13, 0.1, 0.25, 0.12, 0.18, 0.12, 0.07, 0.05, 0.03, 0.02, 0.01].choose; }) * 3;
    pans = Array.fill(12, { [0.0, -0.60, -0.4, 0.4, 0.6].wchoose([0.6, 0.1, 0.1, 0.1, 0.1]); });

    p = Pbind(
      \instrument, \hats,
      \dur, Pseq(durs, inf),
      \amp, Pseq(amps, inf),
      \pan, Pseq(pans, inf)
    );
    r = Pbind(
      \instrument, \hats,
      \dur, 0.25,
      \amp, Pseq(Array.fill(10, { [0.5, 0.3, 0.1].choose; }), inf),
      \pan, Pseq(pans, inf)
    );

    ServerTree.add(~tziharaNodes);
    ~tziharaNodes.value;
    s.sync;
  });

  SynthDef(\tremoloTriangle, {
    var sum = 0;
    var temp, lpfEnv, lpf, tremolo;
    var frequency = \freq.ir(220);
    var duration = \dur.ir(1);
    var attack = \atk.ir(0.01);
    var release = \rel.ir(0.1);
    var env = Env.new([0, 1, 1, 0], [attack, duration, release * duration], [-2, 0, -4]).ar(2);
    frequency.postln;
    4.do {
      var combiFreq = frequency * {Rand(0.99, 1.01)};
      // var ampli = \amp.ir(0.1) * if(600 < combiFreq, { 0.4 }, { 1 });
      var ampli = \amp.ir(0.1);
      temp = LFTri.ar(
        freq: combiFreq,
        iphase: {Rand(0.0, 4.0)}
      );
      sum = sum + Pan2.ar(temp, {Rand(-1.0, 1.0)}, ampli);
    };
    sum = sum * 0.1;
    /*
    lpfEnv = Env.new(
      [{Rand(6271, 9956)} * 0.5, {Rand(5587, 8372)} * 0.4, {Rand(1567, 4186)} * 0.25],
      [{Rand(duration * 0.1, duration * 0.4)}, {Rand(duration * 0.2, duration * 0.4)}, {Rand(duration * 0.1, duration * 0.2)}],
      [-2, 0, 1]
    ).kr(0);
    */
    lpf = RLPF.ar(
      sum,
      [{Rand(4000, 7000)} * 0.5, {Rand(5587, 8372)} * 0.4, {Rand(1567, 4186)} * 0.25, {Rand(1000, 7000)} * 0.125],
      {Rand(0.2, 0.8)}
    ) * env;
    tremolo = LFTri.ar(64 / 60 * 3).range(\depth.ir(0), 1);
    Out.ar(\outBus.ir(0), lpf * tremolo);
    Out.ar(\outFxBus.ir(0), lpf * tremolo * \send.ir(-30).dbamp);
  }).add;

  ~endTremolo = Pbind(
    \instrument, \tremoloTriangle,
    \dur, Pseq([3, 3, 3], 3),
    \freq, Pseq([90, 91, 93].midicps, 3),
    \outBus, 0,
    \outFxBus, 0,
    \atk, 0.1,
    \amp, 0.8
  );
  ~endTremoloTwo = Pbind(
    \instrument, \tremoloTriangle,
    \dur, Pseq([36, 12], 1),
    \freq, Pseq([94, 90].midicps, 1),
    \atk, 0.1,
    \amp, 0.8
  );
)

p.play(TempoClock(64/60));
p.stop;
r.play(TempoClock(64/60));
r.stop;

~endTremolo.play(TempoClock(64/60));

Array.fill(128, { arg i; [1, 2, 2].wchoose([0.2, 0.1, 0.7]); })
[1, 2, [3, 5], 6].flatten;
3!2;
