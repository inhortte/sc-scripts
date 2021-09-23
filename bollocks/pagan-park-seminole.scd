(
  MIDIClient.init;
  ~midiOut = MIDIOut(0);
  ~midiOut.connect(2);
  Routine({
    loop({
      96.do({ |i|
        ~midiOut.control(0, ctlNum: 54, val: i + 16.0);
        (72 / 60 / 4).wait;
      });
      96.do({ |i|
        ~midiOut.control(0, ctlNum: 54, val: 112.0 - i);
        (72 / 60 / 4).wait;
      });
    });
  }).play;
)

s.plotTree;

(
  var eightyFour, seventyTwo, notes1, notes2, notes3, notes4;
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
    SynthDef(\evilSaw, {
      var sig, env, temp, lpfEnv;
      var sum = 0;
      var duration = \dur.ir(1);
      var attack = \atk.ir(0.05);
      env = Env.new(
        [0, 1, 0],
        [attack, duration - attack],
        [-2, 4]
      ).ar(2);
      4.do{
        temp = VarSaw.ar(
          freq: \freq.ir(220) * {Rand(0.99, 1.01)}!2,
          iphase: {Rand(0.0, 1.0)}!2,
          width: {Rand(0.005, 0.05)}!2
        );
        sum = sum + temp;
      };
      lpfEnv = Env.new(
        [{Rand(6500, 8000)}, {Rand(4500, 6000)}, {Rand(4500, 6000)}, {Rand(8500, 12000)}],
        [duration * 0.3333333333, duration * 0.333333333333333, duration * 0.3333333333],
        [-1, 0, -4]
      ).ar(2);
      sum = RLPF.ar(sum, lpfEnv, {Rand(0.2, 0.5)}!2);
      sum = sum * 0.05 * env * \amp.ir(0.1);
      Out.ar(\outBus.ir(0), sum);
      Out.ar(\outFxBus.ir(0), sum * \send.ir(-30).dbamp);
    }).add;

    SynthDef(\reverberation, {
      var sig, wet;
      sig = In.ar(\inBus.ir(0), 2);
      sig = FreeVerb2.ar(sig[0], sig[1], \mix.ir(0.99), \room.ir(0.99), \damp.ir(0.2));
      sig = LPF.ar(sig, \cutoff.ir(1174));  // D6
      Out.ar(\outBus.ir(0), sig);
    }).add;
    
    SynthDef(\dullTriangle, {
      var sum = 0;
      var temp, lpfEnv, lpf;
      var frequency = \freq.ir(220);
      var duration = \dur.ir(1);
      var env = Env.new(
        [0, 1, 0],
        [\atk.ir(0.01), \rel.ir(0.2)],
        [-2, -4]
      ).ar(2);
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
      lpfEnv = Env.new(
        [{Rand(6271, 9956)} * 0.5, {Rand(5587, 8372)} * 0.4, {Rand(1567, 4186)} * 0.25],
        [{Rand(duration * 0.1, duration * 0.4)}, {Rand(duration * 0.2, duration * 0.4)}, {Rand(duration * 0.1, duration * 0.2)}],
        [-2, 0, 1]
      ).kr(0);
      lpf = RLPF.ar(
        sum,
        lpfEnv,
        {Rand(0.2, 0.8)}
      ) * env;
      Out.ar(\outBus.ir(0), lpf);
    }).add;

    SynthDef(\tremoloTriangle, {
      var sum = 0;
      var temp, lpfEnv, lpf, tremolo;
      var frequency = \freq.ir(220);
      var duration = \dur.ir(1);
      var attack = \atk.ir(0.01);
      var release = \rel.ir(0.2);
      var env = Env.new(
        [0, 1, 0],
        [attack, release],
        [-2, -4]
      ).ar(2);
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
      lpfEnv = Env.new(
        [{Rand(6271, 9956)} * 0.5, {Rand(5587, 8372)} * 0.4, {Rand(1567, 4186)} * 0.25],
        [{Rand(duration * 0.1, duration * 0.4)}, {Rand(duration * 0.2, duration * 0.4)}, {Rand(duration * 0.1, duration * 0.2)}],
        [-2, 0, 1]
      ).kr(0);
      lpf = RLPF.ar(
        sum,
        lpfEnv,
        {Rand(0.2, 0.8)}
      ) * env;
      tremolo = SinOsc.kr(
        Env.new(
          [0.1, 12, 0.1],
          [attack, release],
          [-2, -2]
        ).kr(0)
      ).range(\depth.ir(0), 1);
      Out.ar(\outBus.ir(0), lpf * tremolo);
      Out.ar(\outFxBus.ir(0), lpf * tremolo * \send.ir(-30).dbamp);
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

    s.sync;

    ~samplePath = "/flavigula/xian/ppst";
    ~bufs = Dictionary();
    PathName.new(~samplePath).filesDo{|pathName| 
      if("\.wav".matchRegexp(pathName.fullPath) && ("data".matchRegexp(pathName.fullPath)).not) {
        var thurk = pathName.fileNameWithoutExtension;
        pathName.postln;
        ~bufs.put(thurk.asSymbol, Buffer.read(s, pathName.fullPath))
      }
    };

    s.sync;

    ServerTree.add(~tziharaNodes);
    ~tziharaNodes.value;
    s.sync;
  });

  notes1 = [ 60, 74, 66, 68, 60, 74,  60, 74, 66, 68, 60, 74 ];
  notes2 = [ 56, 71, 63, 65, 56, 71, 56, 71, 63, 65, 56, 71 ]; 
  notes3 = [ 59, 72, 64, 66, 59, 72,59, 72, 64, 66, 59, 72 ];
  notes4 = [ 63, 77, 69, 70, 63, 77, 63, 77, 69, 70, 63, 77 ]; 

  seventyTwo = TempoClock(72/60);

  ~a = PmonoArtic(
    \evilSaw,
    \dur, 0.33333333333,
    \atk, Pwhite(0.1, 0.3),
    \midinote, Pseq(notes1, 1),
    \amp, Pwhite(0.37, 0.43, inf),
    \outBus, 0,
    \outFxBus, ~reverbBus,
    \send, Pwhite(-30, -20, inf)
  );
  ~b = PmonoArtic(
    \evilSaw,
    \dur, 0.33333333333,
    \atk, Pwhite(0.1, 0.3),
    \midinote, Pseq(notes2, 1),
    \amp, Pwhite(0.37, 0.43, inf),
    \outBus, 0,
    \outFxBus, ~reverbBus,
    \send, Pwhite(-20, -10, inf)
  );
  ~c = PmonoArtic(
    \evilSaw,
    \dur, 0.33333333333,
    \atk, Pwhite(0.1, 0.3),
    \midinote, Pseq(notes3, 1),
    \amp, Pwhite(0.37, 0.43, inf),
    \outBus, 0,
    \outFxBus, ~reverbBus,
    \send, Pwhite(-20, -10, inf)
  );
  ~d = PmonoArtic(
    \evilSaw,
    \dur, 0.33333333333,
    \atk, Pwhite(0.1, 0.3),
    \midinote, Pseq(notes4, 1),
    \amp, Pwhite(0.37, 0.43, inf),
    \outBus, 0,
    \outFxBus, ~reverbBus,
    \send, Pwhite(-20, -10, inf)
  );
  ~e = Ptpar([0, ~a, 12, ~b, 22, ~c, 38, ~d]);

  ~evilSawPlayer = ~e.play(seventyTwo);
  ~evilSawPlayer.stop;

  eightyFour = TempoClock(84/60);

  // C
  ~part2ChristianC = Pbind(
    \instrument, \bufPlay,
    \outBus, ~reverbBus,
    \outFxBus, ~reverbBus,
    \rate, 0.5,
    \attack, 1,
    \release, 1,
    \sustain, 1,
    \send, -10,
    \buf, Pseq([~bufs["c-no-vibr".asSymbol]], inf),
    \dur, 2.8
  );
  
  // D
  ~part2ChristianD = Pbind(
    \instrument, \bufPlay,
    \outBus, ~reverbBus,
    \outFxBus, ~reverbBus,
    \rate, 0.568,
    \attack, 1,
    \release, 1,
    \sustain, 1,
    \send, -10,
    \buf, Pseq([~bufs["c-no-vibr".asSymbol]], inf),
    \dur, 2.8
  );

  // C#
  ~part2ChristianCis = Pbind(
    \instrument, \bufPlay,
    \outBus, ~reverbBus,
    \outFxBus, ~reverbBus,
    \rate, 0.534,
    \attack, 1,
    \release, 1,
    \sustain, 1,
    \send, -10,
    \buf, Pseq([~bufs["c-no-vibr".asSymbol]], inf),
    \dur, 2.8
  );

  // ending bass
  ~endBass = Pbind(
    \instrument, \dullTriangle,
    \dur, Pseq([20, 60, 5, 35, 10, 10, 10, 15, 5, 10, 25, 15, 5], 1),
    \freq, Pseq([28, 31, 37, 30, 35, 26, 28, 31, 37, 30, 35, 38, 49].midicps, 1),
    \atk, 0.05,
    \rel, Pwhite(3.25, 4.5, inf),
    \amp, 1.2
  );

  // ending tremolo
  ~endTremolo = Pbind(
    \instrument, \tremoloTriangle,
    \dur, Pseq([20, 45, 15, 25, 45, 15, 35, 45, 20], 1),
    \freq, Pseq([76, 79, 85, 78, 83, 86, 88, 90, 85].midicps, 1),
    \atk, Pseq([10, 10, 8, 10, 8, 8, 8, 10, 10], 1),
    \rel, Pseq([10, 10, 7, 10, 7, 7, 7, 10, 10], 1),
    \amp, 0.8
  );
)

c = ~part2ChristianCis.play(TempoClock(84/60));
c.stop;

c = Synth(\bufPlay, [ \buf, ~bufs.at("rrrahhh".asSymbol), \outBus, 0, \outFxBus, ~reverbBus, \rate, 0.5, \send, -10, \amp, 0.9, \attack, 0.01, \sustain, 5, \release, 2 ]);
c = Synth(\bufSlide, [ \buf, ~rrrahhh, \outBus, ~reverbBus, \outFxBus, ~reverbBus, \slideDur, 5, \send, -10, \amp, 0.9, \attack, 0.01, \sustain, 5, \release, 2 ]);


Synth(\evilSaw, [\dur, 4, \freq, 220, \amp, 0.4, \outBus, 0, \outFxBus, ~reverbBus, \send, 0]);

)
Synth(\tremoloTriangle, [ \outBus, 0, \outFxBus, ~reverbBus, \freq, 784, \atk, 10, \rel, 10, \dur, 20, \amp, 0.5, \depth, 0.5, \send, -10]);
Synth(\tremoloTriangle, [ \outBus, 0, \outFxBus, ~reverbBus, \freq, 1175, \atk, 20, \rel, 20, \dur, 40, \amp, 0.5, \depth, 0.5, \send, 10]);
~reverb.set(\room, 0.99);

~rrrahhh = Buffer.read(s, "./ppst/c-no-vibr.wav");
~reverb.set(\room, 0.5);

(
  var eightyFour = TempoClock(84/60);
  p = Pbind(
    \instrument, \dullTriangle,
    \dur, Pwhite(2, 6, 32),
    \midinote, Prand([29, 45, 62, 76, 91, 72, 36, 52, 67, 86, 88, 77], inf),
    \harmonic, Pwrand([1, 2, 3, 5, 7], [0.6, 0.2, 0.1, 0.05, 0.05], inf),
    \atk, Pwhite(1, 4, inf),
    \rel, Pwhite(2, 10, inf),
    \amp, Pwhite(0.6, 0.8, inf) * Pif(Pkey(\midinote) >= 600.cpsmidi, {Pwhite(0.6, 0.8, inf)}, 1)
  ).play(eightyFour);
)
p.stop;

(
  var eightyFour = TempoClock(84/60);
  p = Pbind(
    \instrument, \tremoloTriangle,
    \dur, Prand([4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16], 32),
    \midinote, Prand([52, 52, 91, 55, 49, 85, 95, 83, 90, 54, 86, 50], inf),
    \harmonic, Pwrand([1, 2, 3], [0.6, 0.2, 0.2], inf),
    \atk, Pkey(\dur) * 0.5,
    \rel, Pkey(\dur) * 0.5,
    \amp, Pwhite(0.6, 0.8, inf)
  ).play(eightyFour);
)
