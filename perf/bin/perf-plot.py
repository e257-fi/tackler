#!/usr/bin/python3
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent
#
# Copyright 2016-2018 E257.FI Contributors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
# Plot perf data with Gnuplot
#

import yaml
import argparse
import sys

versions = ["0.4.1", "0.5.0", "0.6.0", "0.7.0", "0.8.0", "0.9.0", "0.10.0"]


def plot_def(hw, testset):
    return """
    #
    #
    set term svg dashed size 2400,800 dynamic background "0xFFFFFF"
    set output "perf-%s-%s.svg"
    set size 1.0,1.0
    set origin 0.0,0.0
    set xtics rotate
    set multiplot
    #
    #
    """ % (hw, testset)


def plot_line_def():
    return """
    plot \
    '-' using 1:2:xtic(1) t "balance (txt, all)"         with linespoints pt 9 lc rgbcolor "0x00FF00" lw 2 dt 1, \
    '-' using 1:2:xtic(1) t "balance-group (txt, all)"   with linespoints pt 9 lc rgbcolor "0x0000FF" lw 2 dt 1, \
    '-' using 1:2:xtic(1) t "register (txt, all)"        with linespoints pt 9 lc rgbcolor "0xFF0000" lw 2 dt 1, \
    '-' using 1:2:xtic(1) t "balance (json, all)"        with linespoints pt 7 lc rgbcolor "0x00FF00" lw 2 dt 2, \
    '-' using 1:2:xtic(1) t "balance-group (json, all)"  with linespoints pt 7 lc rgbcolor "0x0000FF" lw 2 dt 2, \
    '-' using 1:2:xtic(1) t "register (json, all)"       with linespoints pt 7 lc rgbcolor "0xFF0000" lw 2 dt 2, \
    '-' using 1:2:xtic(1) t "balance (txt, flt)"         with linespoints pt 9 lc rgbcolor "0x008800" lw 2 dt 4, \
    '-' using 1:2:xtic(1) t "balance-group (txt, flt)"   with linespoints pt 9 lc rgbcolor "0x000088" lw 2 dt 4, \
    '-' using 1:2:xtic(1) t "register (txt, flt)"        with linespoints pt 9 lc rgbcolor "0x880000" lw 2 dt 4, \
    '-' using 1:2:xtic(1) t "balance (json, flt)"        with linespoints pt 7 lc rgbcolor "0x008800" lw 2 dt 3, \
    '-' using 1:2:xtic(1) t "balance-group (json, flt)"  with linespoints pt 7 lc rgbcolor "0x000088" lw 2 dt 3, \
    '-' using 1:2:xtic(1) t "register (json, flt)"       with linespoints pt 7 lc rgbcolor "0x880000" lw 2 dt 3
    """


def plot_time(testset):
    p_hdr = """
    set size 0.33,1.0
    set origin 0,0
    set grid
    set title "Test set: %s"
    set key top left
    set ylabel "Time (s)"
    set xrange  [*:*]
    set yrange [*:*]
    """ % (testset)

    return p_hdr + plot_line_def()


def plot_mem(testset):
    p_hdr = """
    set size 0.33,1.0
    set origin 0.33,0
    set grid
    set title "Test set: %s"
    set key top left
    set ylabel "Memory (M)"
    set xrange  [*:*]
    set yrange [*:*]
    """ % (testset)

    return p_hdr + plot_line_def()


def plot_cpu(testset):
    p_hdr = """
    set size 0.3,1.0
    set origin 0.66,0
    set grid
    set title "Test set: %s"
    set key top left
    set ylabel "CPU %%"
    set xrange  [*:*]
    set yrange [*:*]
    """ % (testset)

    return p_hdr + plot_line_def()


def values_average(values):
    l = values.get("values")
    assert (len(l) == 5)
    l.sort()

    return sum(l[1:4]) / 3


def gnuplot_version(version, dev=False):
    v = version.split(".")
    if dev:
        return v[0] + "." + "{:02d}".format(int(v[1]) + 1) + "." + v[2] + "-dev"
    else:
        return v[0] + "." + "{:02d}".format(int(v[1])) + "." + v[2]


def values_to_plot(data, key, value_getter, v_func):
    # find result set (times, mem, cpu), based on key triplet (report, format, filter)
    def find_result():
        for run in runs:
            r = run.get("run")
            if r.get("report") == rpt and \
                    r.get("formats") == frmt and \
                    (len(r.get("filter")) != 0) == flt:
                return r

    # get wanted value with value_getter, convert value with v_func and add it to the plot
    def value_to_plot(last):
        try:
            result = find_result()
            value = values_average(value_getter(result.get("result"), key))
            return gnuplot_version(v, last) + "  " + "{:.2f}".format(v_func(value)) + "\n"
        except AttributeError:
            return ""

    result_str = ""
    for flt in [False, True]:
        for frmt in ["txt", "json"]:
            for rpt in ["balance", "balance-group", "register"]:
                for v in versions:
                    version_data = data.get(v)
                    if version_data:
                        runs = data.get(v).get("runs")
                        result_str += value_to_plot(False)

                # duplicate last data point, so resulting plot with straight line will be easier to read
                # than single dot at the right most end of plot.
                # There could be some option / different plot style with gnuplot, but let's go with that now
                runs = data.get(versions[-1]).get("runs")
                result_str += value_to_plot(True)
                result_str += "e\n"

    return result_str


def main():
    argp = argparse.ArgumentParser(description="tackler perf data plotter")

    argp.add_argument("basedir", help="path to basedir")
    argp.add_argument("hw", help="Test hardware")
    argp.add_argument("set", help="1E3 ...")
    args = argp.parse_args()

    data = dict()
    for v in versions:
        data_file = args.basedir + "/" + args.hw + "/" + args.hw + "-" + v + "-" + args.set + ".yml"
        try:
            with open(data_file, "r") as f:
                data[v] = yaml.load(f.read())
        except FileNotFoundError:
            data[v] = None

    print(plot_def(args.hw, args.set))

    print(plot_time(args.set))
    print(values_to_plot(data, "real",
                         lambda z, key: z.get("times").get(key),
                         lambda x: x))

    print(plot_mem(args.set))
    print(values_to_plot(data, "mem",
                         lambda z, key: z.get(key),
                         lambda x: x / 1024))

    print(plot_cpu(args.set))
    print(values_to_plot(data, "cpu",
                         lambda z, key: z.get(key),
                         lambda x: x))

    print("unset multiplot")


if __name__ == "__main__":
    main()
