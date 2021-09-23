/*
(
  s.options.numBuffers = 8192; 
  s.boot;
)
*/
s.plotTree;
s.meter;

(
t = TempoClock(72/60).permanent_(true);

// Busses
~txiharaBusses = {
  s.newBusAllocators;
  ~bus = Dictionary.new;
  ~bus.add(\reverb -> Bus.audio(s, 2));
  ~bus.add(\comp -> Bus.audio(s, 2));
};

// Groups
~txiharaNodes = {
  s.bind({
    ~src = Group.new;
    ~efx = Group.after(~src);
    ~reverb = Synth(\reverb, [
        \in, ~bus[\reverb], 
        \out, ~bus[\comp] 
      ],
      ~efx
    );
    ~comp = Synth(\comp,
      [ \in, ~bus[\comp], \out, 0 ],
      ~efx,
      \addToTail
    );
  });
};

~cleanup = {
  s.newBusAllocators;
  ServerBoot.removeAll;
  ServerTree.removeAll;
  ServerQuit.removeAll;
};

ServerBoot.add(~txiharaBusses);
ServerQuit.add(~cleanup);

s.waitForBoot({
  s.sync;
  SynthDef(\dullSaw, {
    arg outBus = 0, freq = 220, descendTo = 196, dur = 1, amp = 0.1;
    var sum = 0;
    var curFreq, targetFreq, freqLine, temp, lag;
    var env = EnvGen.kr(
      Env.new(
        [0, 1, 1, 0],
        [dur * 0.2, dur * 0.7, dur * 0.1],
        curve: [-1, 0, -2]
      ),
      levelScale: amp,
      doneAction: 2
    );
    3.do{
      curFreq = freq * {Rand(0.99, 1.01)}!2;
      targetFreq = descendTo * {Rand(0.99, 1.02)}!2;
      freqLine = EnvGen.kr(
        Env.new(
          [ curFreq, curFreq, targetFreq ],
          [ dur * 0.5, dur * 0.5],
          [0, 0]
        )
      );
      // freqLine = Line.ar(curFreq, targetFreq, dur * 0.5) + Line.ar(curFreq, curFreq, dur * 0.5);
      temp = VarSaw.ar(
        freq: freqLine,
        iphase: {Rand(0.0, 1.0)}!2,
        width: {Rand(0.005, 0.05)}!2
      );
      sum = sum + temp;
    };
    sum = sum * 0.05 * env;
    Out.ar(outBus, sum);
  }).add;

  SynthDef(\dullTriangle, {
    arg outBus = 0, freq = 220, atk = 0.01, rel = 0.2, dur = 1, amp = 0.1;
    var sum = 0;
    var temp;
    var env = EnvGen.kr(
      Env.new(
        [0, 1, 0],
        [atk, rel],
        [-2, -4]
      ),
      levelScale: amp,
      doneAction: 2
    );
    4.do {
      freq = freq * {Rand(0.99, 1.01)};
      temp = LFTri.ar(
        freq: freq,
        iphase: {Rand(0.0, 4.0)}
      );
      sum = sum + Pan2.ar(temp, {Rand(-1.0, 1.0)}, 0.5);
    };
    sum = sum * 0.1 * env;
    Out.ar(outBus, sum);
  }).add;
});
)

(
y = Synth(\dullSaw, [ dur: 15, amp: 0.05 ]);
y.run(false);
)
y.run(true);
x = Synth(\dullSaw, [ freq: 1397, descendTo: 1300, dur: 5, amp: 0.05 ]);
x.set(\freq, 1358, \lagTime, 5);
x.free;

(
  var durs = [8, 8, 4, 4, 4, 4, 4];
  var freqs = [1397, 1175, 1318, 1244.5, 1175, 1397, 1046.5];
  var descendTos = [1357, 1141, 1281, 1209, 1141, 1357, 1281];
  var durs2 = [6, 8, 8, 4, 8];
  var freqs2 = [ 1109, 1357, 1175, 1318, 988 ];
  var descendTos2 = [ 1077, 1357, 1141, 1281, 1046.5 ];
  /*
  var durs = [ 
    8, 8, 8, 4,
    2, 1.5, 0.5, 4,
    2, 1.5, 0.5, 4,
    2, 1.5, 0.5, 0.6666666667, 0.666666667, 0.666666667, 0.25, 0.25, 1.5, 4,
    1.5, 0.5, 0.666666667, 0.666666667, 0.666666667, 4,
    1.5, 0.5, 0.666666667, 0.666666667, 0.666666667, 4,
    0.5, 7.5
  ];
  var freqs = [
    1397, 1175, 1318, 988,
    698.5, 784, 622, 523,
    698.5, 784, 622, 587,
    698.5, 494, 415, 466, 440, 392, 523, 554, 523, 587,
    698.5, 880, 1141, 1281, 784, 523,
    880, 1175, 1318, 1568, 1046.5, 698.5,
    587, 659
  ];
  var descendTos = [
    1357, 1141, 1281, 784,
    679, 762, 604.5, 508.5,
    679, 762, 604.5, 570.5,
    679, 480, 410, 460, 432, 385, 523, 554, 508.5, 570.5,
    679, 855, 1155, 1298, 775, 508.5,
    855, 1170, 1298, 1500, 1035, 679,
    623, 640.5
  ];
  */
  Pbind(
    \instrument, \dullSaw,
    \freq, Pseq(freqs , 1),
    \descendTo, Pseq(descendTos, 1),
    \amp, 0.2,
    \dur, Pseq(durs, 1)
  ).play(t);
)

(
  var eightyFour = TempoClock(84/60);
  p = Pbind(
    \instrument, \dullTriangle,
    \dur, Pwhite(0.2, 3.2, inf),
    \freq, Prand([1397, 1175, 698.5, 587, 1318, 659, 784, 1568, 523, 1046.5, 440, 880] * 0.5, inf),
    \atk, Pwhite(0.01, 4, inf),
    \rel, Pwhite(0.3, 4, inf)
  ).play(eightyFour);
)

