#!/usr/bin/env python3
"""Run the test client on an isolated, silent X display (inside nix develop)."""
import os
import subprocess
import sys

with subprocess.Popen(
    ["Xvfb", "-displayfd", "1", "-screen", "0", "1280x720x24", "-nolisten", "tcp"],
    stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True,
) as display:
    try:
        number = display.stdout.readline().strip()
        if not number.isdigit():
            raise SystemExit("Xvfb failed to allocate a display")
        env = os.environ.copy()
        env["DISPLAY"] = ":" + number
        env["SDL_VIDEO_DRIVER"] = "x11"
        env.pop("WAYLAND_DISPLAY", None)
        result = subprocess.run(sys.argv[1:], env=env)
    finally:
        display.terminate()
        display.wait(timeout=10)
raise SystemExit(result.returncode)
