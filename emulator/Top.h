#ifndef __TOP__
#define __TOP__

#include "gui.h"
#include "gfx.h"
#include "viz.h"
#include "vec.h"
#include "lay.h"
#include "vec.h"
#include "lisp.h"
#include "reader.h"
#include "timer.h"
#include <unistd.h>
#include <stdio.h>
#include "emulator.h"
#include "Music.h"
#include <iostream>
#include <fstream>

using namespace std;

class sim_viz_t;

class sim_t {
 public:
  Music_t* music;
  uint64_t ticks;
  int render (int is_picking);
  sim_t* exec ( sim_viz_t* viz );
  void open ( void );
  void close ( void );
  sim_t ( void );
};

class sim_viz_t : public viz_t {
 public:
  int key_hit (int cmd, int modifiers);
  int exec (int is_pause);
  int render (int is_picking);
  int render_frame_monitors ( void );
  int handle_drag(vec_t<3> pos);
  int process_picks (std::vector< int > picks);
  int open (int arg_offset, int argc, const char *argv[]);
  int close (void);

  void show_status (float x, float y, float w, float h);
};

extern int is_queues;

#endif
