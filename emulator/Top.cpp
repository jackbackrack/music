#include "Top.h"
#include "Music.h"

sim_t *sim;

extern std::string user_msg;

props_t* props;

defboolprop(is_show_status, true);
defstrprop(filename,  "counter2.flo"); 
defnumpropmod(flo, accum_fade,  0.0, 0.1, 0.0, 1.0); 
defnumpropmod(flo, accum_value, 1.0, 0.1, 0.0, 1.0); 
defnumpropmod(int, num_execs, 1, 1, 1, 1000); 
defnumpropmod(int, num, 8, 1, 0, 1000); 

vec_t<3> hsv_to_rgb(vec_t<3> hsv) { 
  double r, g, b;
  hsv_to_rgb(hsv.x, hsv.y, hsv.z, &r, &g, &b);
  // post("H [%f,%f,%f] XYZ [%f,%f,%f]\n", hsv.x, hsv.y, hsv.z, rgb.x, rgb.y, rgb.z);
  return vec(r, g, b);
}

vec_t<3> rgb_to_hsv(vec_t<3> rgb) { 
  vec_t<3> hsv;
  rgb_to_hsv(rgb.x, rgb.y, rgb.z, &hsv.x, &hsv.y, &hsv.z);
  return hsv;
}


void sim_t::open (void) {
  ticks      = 0;
}

void sim_t::close (void) {
  // DELETE CELLS
}

int sim_t::render (int is_picking) {
  return 1;
}

sim_t* sim_t::exec ( sim_viz_t *viz ) {
  dat_t<1> reset = LIT<1>(ticks == 0);
  // for (size_t i = 0; i < viz->n_aud_frames; i++) {
    // music->Music__io_in_sample_l.values[0] = fromFloat(viz->in_samples[0][i]);
    // music->Music__io_in_sample_r.values[0] = fromFloat(viz->in_samples[1][i]);
    music->Music__io_t.values[0] = fromDouble(viz->audio_tick);
    music->clock_lo(reset);
    music->clock_hi(reset);
    // viz->out_samples[0][i] = toFloat(music->Music__io_out_0.values[0]);
    // viz->out_samples[1][i] = toFloat(music->Music__io_out_1.values[0]);
    viz->speakers[0] = toDouble(music->Music__io_out_0.values[0]);
    viz->speakers[1] = toDouble(music->Music__io_out_1.values[0]);
  // }
  ticks += 1;
}

int sim_viz_t::exec (int is_pause) {
  sim->exec(this);
}

int sim_viz_t::handle_drag (vec_t<3> pos) {
}

int sim_viz_t::process_picks (std::vector< int > picks) {
}

keys_t* top_keys;
cmds_t* top_cmds = new cmds_t();
cmds_t* cmds;

void install_keys (void) {
  now_keys = top_keys = new keys_t(key_not_found);
  top_keys->install("z", reset_view_cmd);
  top_keys->install("q", quit_cmd);
  top_keys->install("f", toggle_full_screen_cmd);
  top_keys->install("s", single_step_cmd);
  top_keys->install("x", resume_execution_cmd);
}

int sim_viz_t::key_hit (int cmd, int modifiers) {
  user_msg.clear();
  is_key_hit[cmd] = 1;
  key_modifiers = modifiers;
  now_keys->do_process_keys(cmd, 0, key_modifiers, sim);
}

void sim_viz_t::show_status (float x, float y, float w, float h) {
  char text[100];
  sprintf(text, "%s", user_msg.c_str());
  glPushMatrix(); glPushAttrib(GL_CURRENT_BIT);
  glColor3f(1, 0, 1);
  glTranslatef( MIN_X+25, MIN_Y+2, 5); 
  draw_text(50, 10, text);
  glPopAttrib(); glPopMatrix();
  glPushMatrix(); glPushAttrib(GL_CURRENT_BIT);
  if (0) {
  glColor3f(1, 0, 1);
  char mode[100];
  // sprintf(mode, "L %.2f C %.2f P %.2f S %.2f N %.2f W %.2f", sim->fabric->comm_len, spring, pressure, sigma, noise, wall);
  // glTranslatef( MIN_X+25, MIN_Y+2, 5); 
  // glTranslatef( x - 50, y, 5); 
  // draw_text(100, 10, mode);
  draw_text(w * 2, h, mode);
  // draw_text(w, h, text);
  }
  glPopAttrib(); glPopMatrix();
}

int sim_viz_t::render_frame_monitors ( void ) {
  if (is_show_status) {
    show_status(viz->MAX_X-15, viz->MIN_Y+2, 30, 20);
  }
}

int sim_viz_t::render (int is_picking) {
  if (is_process_frame) {
    int is_rendered = sim->render(is_picking);
    if (accum_fade != 0.0 && accum_value != 1.0) {
      glAccum(GL_ACCUM, accum_value);
      glAccum(GL_RETURN, 1);
      glAccum(GL_MULT, accum_fade);
      // post("ACCUM %f %f\n", num_data(accum_value), num_data(accum_fade));
    }
    return is_rendered;
  } else
    return false;
}

viz_t* new_viz (void) {
  viz_t* viz = new sim_viz_t();
  return viz;
}

static int install_props (void) {
  props = new props_t();
  props->install(num_var);
  props->install(num_execs_var);
}

static void sim_parse_args (int argc, const char *argv[]) {
  std::vector<const char*> args;
  for (int i = 1; i < argc; i++) {
    args.push_back(argv[i]);
  }
  std::vector<const char*>::iterator ap  = args.begin();
  std::vector<const char*>::iterator eap = args.end();
  props->parse_args(ap, eap, sim);
  std::string fn(argv[argc-1]);
  filename = fn;
}


sim_t::sim_t ( void ) { }

int sim_viz_t::open(int args_offset, int argc, const char** argv) {
  string str_in;
  sim = new sim_t();
  install_props();
  install_keys();
  sim_parse_args(argc, argv);
  init_gfx();
  sim->music = new Music_t();
  sim->open();
}

int sim_viz_t::close ( void ) {
  sim->close();
  // sim->close();
  return 0;
}
