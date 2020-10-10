(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

b = Buffer.read("samples/Bbma7-sample.wav");

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


