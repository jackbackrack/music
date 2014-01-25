CC = gcc
CXX = g++-4.8

OS2 := $(strip $(shell uname))
GUI = $(GUI_HOME)
ifeq ($(OS2), Darwin)
  # Mac OSX
  $(info Compiling for Darwin)
  LIBS = $(GUI)/app.a  -lobjc -lode -framework GLUT -framework OpenGL -lopencv_core -lopencv_highgui -lopencv_video -lopencv_imgproc -lopencv_objdetect -lopencv_calib3d -L/usr/local/lib -L/opt/local/lib -lportaudio -lportmidi #-lsolid #-lglaux # -pg # -framework Foundation
  #HLLIBS =
  CPPFLAGS=-I.. -I/opt/local/include -I/usr/local/include -I/opt/local/include/OpenEXR -I/usr/local/include/SOLID -I$(GUI) -I$(CHISEL)/csrc -D IS_AUDIO=1
  CXXFLAGS=-std=c++11 -Wno-write-strings -g -O3  -D MACOSX=1 -D IS_ODE=1  # -pg  -flat_namespace
else
  OS := $(strip $(shell uname -o))
  ifeq ($(OS), GNU/Linux)
    $(info Compiling for GNU/Linux)
    CPPFLAGS = -I..  -I/usr/local/include/OpenEXR -I$(GUI) -I$(CHISEL)/csrc # -I/usr/include/opencv -I/usr/local/include/opencv
    LIBS = $(GUI)/app.a  -lglut -lGL -lGLU -lm #  -lcv -lcvaux -lhighgui -L"/usr/local/share/ipp/sharedlib"  -lippi -lippcore -lippcv -lippcc -lguide -lippvm # -lglaux # -pg
    HLLIBS = -lm
    CXXFLAGS = -g -Wno-write-strings -D IS_ODE=1# -pg  -O3
  else
    ifeq ($(OS), Cygwin)
      # Cygwin under Windows
      $(info Compiling for Cygwin)
      CPPFLAGS =  $(DEFS) -Ic:/jrb/ode-0.8/include -I c:/progra~1/opencv/cxcore/include  -Ic:/progra~1/opencv/cv/include -Ic:/progra~1/opencv/cvaux/include -Ic:/progra~1/opencv/otherlibs/highgui -I$(CHISEL)/csrc
      SLIBS = c:/jrb/ode-0.8/ode/src/libode.a  c:/progra~1/opencv/lib/cv.lib c:/progra~1/opencv/lib/cvaux.lib c:/progra~1/opencv/lib/highgui.lib c:/progra~1/opencv/lib/cxcore.lib
      LIBS =  $(SLIBS) libportaudio.win32.a libportmidi.win32.a -lglut32 -lopengl32 -lglu32 -lglaux -lwinmm -lgdi32 -lstdc++ # -pg
      #HLLIBS =
      CXXFLAGS = -O3 -g -DIS_CV# -pg
    else
      # unknown; fail noisily
      $(error Unknown operating system '$(OS)'; please rerun make with OS set to 'GNU/Linux', 'Darwin', or 'Cygwin')
    endif
  endif
endif

ALL = music

all: $(ALL)

clean:
	rm -f *.o $(ALL)

music: %: Top.o Music.o $(GUI)/app.a 
	g++ $(LDFLAGS) -o $@ Top.o Music.o $(LIBS)

Top.o: Top.h Top.h Top.cpp Music.h
	g++ -c $(CXXFLAGS) $(CPPFLAGS) Top.cpp

Music.cpp: ../src/Music.scala
	(cd ../sbt; sbt "project music" "run music")

Music.o: $(CHISEL)/csrc/emulator.h Music.h Music.cpp
	g++ -c $(CXXFLAGS) $(CPPFLAGS) Music.cpp

