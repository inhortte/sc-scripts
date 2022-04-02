(
  s.plotTree;
  s.meter;
  s.scope;
)
(
  ~repeatOneOfThree = {
    arg arr;
    var choice = [0, 1, 2].choose;
    Array.fill(3, { |idx|
      if(choice == idx,
        [arr.at(idx), arr.at(idx)],
        arr.at(idx));
    });
  };
  ~makeDurs = {
    arg arr;
    Array.fill(3, { |idx|
      if(arr.at(idx).class == Array,
        [0.16666666666667, 0.16666666666667],
        0.33333333333333);
    });
  };
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;

    ~chrisBufs = List();
    "/home/polaris/flavigula/xian/ramblings-to-krzys-slices/thurk*.wav".pathMatch.collect { |file|
      ~chrisBufs.add(Buffer.readChannel(s, file, channels: [0]));
    };
    ~silenceBuf = Buffer.readChannel(s, "/home/polaris//flavigula/xian/ramblings-to-krzys-slices/silence.wav", channels: [0]);
    ~backdropBufL = Buffer.readChannel(s, "/home/polaris/flavigula/blows/algorave-back.wav", channels: [0]);
    ~backdropBufR = Buffer.readChannel(s, "/home/polaris/flavigula/blows/algorave-back.wav", channels: [1]);
    ~bBakFiles = List();
    ~bBakBufs = List();
    ~aBakFiles = List();
    ~aBakBufs = List();
    ~bFiles = List();
    ~bBufs = List();
    ~aFiles = List();
    ~aBufs = List();
    "/home/polaris/flavigula/ardour/vegkoref/export/b-bak-[12].wav".pathMatch.collect { |file|
      ~bBakFiles.add(PathName(file).fileName);
      ~bBakBufs.add(Buffer.read(s, file));
    };
    "/home/polaris/flavigula/ardour/vegkoref/export/a-bak-[12].wav".pathMatch.collect { |file|
      ~aBakFiles.add(PathName(file).fileName);
      ~aBakBufs.add(Buffer.read(s, file));
    };
    "/home/polaris/flavigula/ardour/vegkoref/export/b-[01][0-9].wav".pathMatch.collect { |file|
      ~bFiles.add(PathName(file).fileName);
      ~bBufs.add(Buffer.read(s, file));
    };
    "/home/polaris/flavigula/ardour/vegkoref/export/a-[0-9].wav".pathMatch.collect { |file|
      ~aFiles.add(PathName(file).fileName);
      ~aBufs.add(Buffer.read(s, file));
    };

    ~melodies1 = List();
    ~melodies2 = List();
    ~melodies1.add([59, 66, 62, 69, 66, 73].midicps); // b fis d a fis cis
    ~melodies1.add([59, 64, 61, 67, 64, 71].midicps); // b e cis g e b
    ~melodies1.add([57, 64, 59, 66, 62, 69].midicps); // a e b fis d a
    ~melodies1.add([55, 64, 66, 61, 57, 61].midicps); // g e fis cis a cis
    ~melodies2.add([59, 65, 62, 69, 65, 72].midicps); // b f d a f c
    ~melodies2.add([59, 64, 60, 68, 64, 71].midicps); // b e c gis e b
    ~melodies2.add([57, 64, 59, 65, 62, 69].midicps); // a e b f d a
    ~melodies2.add([56, 64, 65, 60, 57, 60].midicps); // gis e f c a c

    g = Group.basicNew(s, 1);
    j = Group.basicNew(s, 1);
    d = Bus.audio(s, 2);
    e = Bus.audio(s, 2);
    SynthDef(\trianglish, {
      var dur = \dur.ir(1.0);
      var freq = \freq.ir(147);
      var env = Env.perc(
        attackTime: 0.1,
        releaseTime: dur * 1.2,
        level: 1,
        curve: -3
      ).kr(2);
      var tri = LFTri.ar(
        freq: freq,
        iphase: [0, 1.3, 2.9, 3.2],
        mul: \amp.ir(0.1) * \mult.ir(1.0)
      ) * env;
      var lpf = RLPF.ar(
        in: tri,
        freq: Line.kr(freq * \highMul.ir(4), freq / 4, dur * 0.8),
        rq: \rq.ir(0.2)
      );
      Out.ar(
        \outBus.ir(0),
        Splay.ar(lpf)
      );
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
    SynthDef(\pleasantPulse, {
      var freq = \freq.ir(220);
      var env = Env.perc(\attack.ir(0.05), \dur.ir(1.0) * \release.ir(1.2), curve: -2).kr(2);
      var pulse = LFPulse.ar(
        freq: [ freq, freq * 0.995, freq * 1.005 ],
        iphase: [ 0.2, 0.4, 0.53, 0.95 ],
        width: SinOsc.kr(0.2).range(-0.3, 0.3)
      ) * env * \amp.ir(0.1);
      Out.ar(\outBus.ir(0), Splay.ar(pulse));
    }).add;
    SynthDef(\lpgPulse, {
      var freq = \freq.ir(220);
      var env = Env.perc(\attack.ir(0.001), \release.ir(0.02), curve: -5).kr(2);
      var pulse = LFPulse.ar(
        freq: [ freq, freq * 0.99, freq * 1.01 ],
        iphase: [ 0.0, 0.3, 0.6, 0.8 ],
        width: SinOsc.kr(0.6).range(-0.6, 0.6)
      ) * env * \amp.ir(0.1);
      var lpf = LPF.ar(pulse, SinOsc.kr(0.3).range(880, 1250));
      Out.ar(\outBus.ir(0), Splay.ar(lpf));
    }).add;
    SynthDef(\reverberation, {
      var sig, wet;
      sig = In.ar(\inBus.ir(0), 2);
      sig = FreeVerb2.ar(sig[0], sig[1], \mix.ir(0.4), \room.ir(0.60), \damp.ir(0.2));
      sig = LPF.ar(sig, [SinOsc.kr(0.05).range(880, 1220), 1174]);
      // sig = LPF.ar(sig, \cutoff.ir(1174));  // D6
      Out.ar(\outBus.ir(0), Splay.ar(sig));
    }).add;
    SynthDef(\bongHit, {
      var delay = \delay.ir(0.75);
      var pan = LFNoise0.kr(2.75).range(-0.8, 0.8);
      var sig = CombN.ar(
        In.ar(\inBus.ir(0), 2),
        delay,
        delay,
        SinOsc.kr(0.1).range(0.5, 2.5)
      );
      Out.ar(
          \outBus.ir(0),
          Pan2.ar(sig, pan)
      );
    }).add;

    SynthDef(\bufplay, {
      arg outBus = 0, bufNum = 0, startPos = 0, dur = 1.0;
      var sig, endPos, lb, idx;
      var env = Env.perc(attackTime: 0.01, releaseTime: dur * 0.9, curve: -2.0);

      startPos = startPos * BufSampleRate.kr(bufNum);

      sig = PlayBuf.ar(
        numChannels: 2, 
        bufnum: bufNum, 
        rate: BufRateScale.kr(bufNum), 
        startPos: startPos, 
        loop: 0,
        doneAction: Done.freeSelf
      );
      Out.ar(outBus, sig );
    }).add;
 
    SynthDef(\grainy, {
      var sig, buf = \buf.ir(~backdropBufL);
      sig = GrainBuf.ar(
        numChannels: 2,
        // trigger: Impulse.kr({ExpRand(60, 60.9)}!12),
        // trigger: Impulse.ar(128),
        trigger: Dust.kr(\dustFreq.ir(128)),
        // dur: Line.kr(1/8, 1/64, BufDur.kr(buf)),
        dur: LFNoise1.kr(\noiseFreq.ir(24)).range(\low.ir(1/128), \high.ir(1/32)),
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
        pan: LFNoise1.kr(\panFreq.ir(128)).range(-1, 1),
        // pan: 0,
        envbufnum: -1,
        maxGrains: 512,
        mul: \amp.ir(0.4)
      );
      sig = Splay.ar(sig);
      Out.ar(\outBus.ir(0), sig);
    }).add;
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


    ~bassDurs = [ 
      0.5, 0.5, 1.5, 0.5,
      0.5, 0.5, 0.5, 0.5, 1.5,
      0.5, 0.5, 1.5
    ];
    ~bassBufs = [
      ~bBufs, ~bBufs, ~bBufs, ~bBufs,
      ~bBufs, ~aBufs, ~bBufs, ~aBufs, ~bBufs,
      ~bBufs, ~bBufs, ~bBufs,
      ~bBakBufs, ~aBakBufs
    ];
    ~hatsAmps = Array.fill(128, { [0.2, 0.13, 0.1, 0.25, 0.12, 0.18, 0.12, 0.07, 0.05, 0.03, 0.02, 0.01].choose; }) * 12;
    ~hatsPans = Array.fill(12, { [0.0, -0.60, -0.4, 0.4, 0.6].wchoose([0.6, 0.1, 0.1, 0.1, 0.1]); });

    ~bass = Pbind(
      \instrument, \bufplay,
      \dur, 8, // Pseq(~bassDurs, inf),
      \bufNum, Pseq(~bassBufs, inf).collect({ |bufs| bufs.choose })
    );
    ~chris = Pbind(
      \instrument, \grainy,
      \group, g,
      \addAction, 0,
      \dustFreq, Pwhite(24, 98, inf),
      \noiseFreq, Pwhite(32, 96, inf),
      \amp, 0.9,
      \buf, Pseq(Array.fill(119, { [~chrisBufs.choose, ~silenceBuf].wchoose([0.1, 0.9]) }), 1)
    );
    ~triDurs = Array.fill(64, { [2, 4, 8, 0.5, 1, 1.333333333, 0.66666666666].wchoose([0.4, 0.2, 0.1, 0.1, 0.05, 0.05, 0.05]) });
    ~triDurs2 = Array.fill(64, { [4, 8, 0.5, 1].wchoose([0.4, 0.4, 0.1, 0.1]) });
    ~tri = Pbind(
      \instrument, \trianglish,
      \group, g,
      \addAction, 0,
      \outBus, d,
      \amp, Prand([0.6, 0.3, 0.4, 0.5], inf),
      \mult, 0.7,
      \freq, Pseq(Array.fill(127, { ~melodies1.choose; }).flatten, Prand([1, 2, 3], 1)),
      \dur, Pseq(~triDurs, inf)
    );
    ~tri2 = Pbind(
      \instrument, \trianglish,
      \group, g,
      \addAction, 0,
      \outBus, d,
      \amp, Prand([0.5, 0.2, 0.3, 0.4], inf),
      \mult, 0.7,
      \rq, 0.1,
      \highMul, 3.0,
      \freq, Pseq(Array.fill(127, { ~melodies2.choose; }).flatten, Prand([1, 2, 3], 1)),
      \dur, Pseq(~triDurs2, inf)
    );
    ~hats = Pbind(
      \instrument, \hats,
      \group, g,
      \addAction, 0,
      \outBus, 0,
      \dur, Prand([2.02, 2.0, 1.98, 2.06, 1.94], inf),
      \amp, Pseq(~hatsAmps, inf),
      \pan, Pseq(~hatsPans, inf)
    );
    ~hats2 = Pbind(
      \instrument, \hats,
      \group, g,
      \addAction, 0,
      \outBus, 0,
      \dur, Pwrand([0.25, 1.0, 0.5], [0.2, 0.5, 0.3], inf),
      \amp, Prand([0.9, 0.7, 0.8], inf),
      \pan, Pseq(~hatsPans, inf)
    );
    ~pulse = Pbind(
      \instrument, \pleasantPulse,
      \group, g,
      \addAction, 0,
      \outBus, d,
      \amp, 0.3,
      \freq, Pseq([49, 49, 90].midicps, inf),
      \dur, Pseq([0.25, 2.75, 13], inf),
      \attack, Pseq([0.001, 0.03, 0.5], inf),
      \release, Pseq([0.4, 0.8, 0.5], inf)
    );
    ~pulseF = Pbind(
      \instrument, \pleasantPulse,
      \group, g,
      \addAction, 0,
      \outBus, d,
      \amp, 0.3,
      \freq, Pseq([48, 48, 89].midicps, inf),
      \dur, Pseq([0.25, 2.75, 13], inf),
      \attack, Pseq([0.001, 0.03, 0.5], inf),
      \release, Pseq([0.4, 0.7, 0.4], inf)
    );
    ~lpg1Melody = [79, 74, 73].midicps * 2;
    ~lpg1Freqs = Array.fill(128, {
      if(0.125.coin,
        ~repeatOneOfThree.(~lpg1Melody),
        ~lpg1Melody);
    });
    ~lpg1Durs = Array.fill(128, { |idx|
      ~makeDurs.(~lpg1Freqs.at(idx))
    });
    ~lpg2Melody = [80, 77, 76].midicps * 2;
    ~lpg2Freqs = Array.fill(128, {
      if(0.125.coin,
        ~repeatOneOfThree.(~lpg2Melody),
        ~lpg2Melody);
    });
    ~lpg2Durs = Array.fill(128, { |idx|
      ~makeDurs.(~lpg2Freqs.at(idx))
    });
    ~lpg2Freqs.postln;
    ~lpg2Durs.postln;
    ~lpgPulse = Pbind(
      \instrument, \lpgPulse,
      \group, g,
      \outBus, e,
      \addAction, 0,
      \amp, 0.9,
      // \amp, Prand([0.5, 0.6, 0.4], inf),
      \dur, Pseq(~lpg1Durs.flatten(2), inf),
      \freq, Pseq(~lpg1Freqs.flatten(2), inf)
    );
    ~lpgPulse2 = Pbind(
      \instrument, \lpgPulse,
      \group, g,
      \outBus, e,
      \addAction, 0,
      \amp, 0.9,
      // \amp, Prand([0.5, 0.6, 0.4], inf),
      \dur, Pseq(~lpg2Durs.flatten(2), inf),
      \freq, Pseq(~lpg2Freqs.flatten(2), inf)
    );
  });
)

Synth.tail(g, \bongHit, [ outBus: 0, inBus: e ]);
Synth.tail(g, \reverberation, [ outBus: 0, inBus: d ]);
m = Synth.head(g, \grainy, [\amp, 0.5])
n = Synth.head(g, \grainy, [\buf, ~backdropBufR, \dustFreq, 256, \noiseFreq, 48, \amp, 0.4]);
~chris.play(TempoClock(12/60));
(
  t = ~tri.play;
  t.mute;
  u = ~tri2.play;
  u.mute;
  h = ~hats.play;
  h.mute;
  i = ~hats2.play;
  i.mute;
  k = ~bass.play;
  k.mute;
  p = ~pulse.play;
  p.mute;
  r = ~pulseF.play;
  r.mute;
  q = ~lpgPulse.play;
  q.mute;
  v = ~lpgPulse2.play;
  v.mute;
)
t.mute
t.unmute;
u.mute;
u.unmute;
h.mute;
h.unmute;
k.mute;
k.unmute; // bass
p.mute;
p.unmute; // Pulse
r.mute;
r.unmute; // Pulse 2
q.mute; // lpg 1
q.unmute;
v.mute; // lpg 2
v.unmute;
Synth(\evilSaw, [\dur, 12, \freq, 38.midicps, \amp, 0.4, \outBus, 0, \outFxBus, d, \send, 0]); // d
Synth(\evilSaw, [\dur, 12, \freq, 35.midicps, \amp, 0.4, \outBus, 0, \outFxBus, d, \send, 0]); // b
Synth(\evilSaw, [\dur, 12, \freq, 33.midicps, \amp, 0.4, \outBus, 0, \outFxBus, d, \send, 0]); // a
Synth(\evilSaw, [\dur, 12, \freq, 32.midicps, \amp, 0.4, \outBus, 0, \outFxBus, d, \send, 0]); // gis
