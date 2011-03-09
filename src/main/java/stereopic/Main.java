/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package stereopic;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Main {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);
    private final static ResourceBundle bundle = ResourceBundle.getBundle("messages");
    private static void printHelpAndExit(Options opt){
        HelpFormatter f = new HelpFormatter();
        f.printHelp("java stereopic.Main [options] filePath or directory", opt);
        System.exit(-1);
    }

    public static void main(String args[]) throws IOException {
        Options opt = new Options();
        opt.addOption("h", false, bundle.getString("h"));
        opt.addOption("gif", false, bundle.getString("gif"));
        opt.addOption("si", false, bundle.getString("si"));
        opt.addOption("width", true, bundle.getString("width"));
        opt.addOption("delay", true, bundle.getString("delay"));
        BasicParser parser = new BasicParser();
        CommandLine cl = null;
        try {
            cl = parser.parse(opt, args);
            if (cl.hasOption('h')) {
                printHelpAndExit(opt);
            }
        } catch (ParseException e) {
            printHelpAndExit(opt);
        }
        if (args.length < 1) {
            printHelpAndExit(opt);
        }
        File file = new File(args[0]);
        if (file.isDirectory()) {
            FileFilter ff = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.getName().toUpperCase().endsWith(".MPO");
                }
            };
            File[] files = file.listFiles(ff);
            if (files.length == 0) {
                LOG.warn("No .MPO file found.");
                System.exit(-1);
            } else {
                for (File mpo : files) {
                    process(mpo.getAbsolutePath(), cl);
                }
            }
        } else if (!file.exists()) {
            LOG.warn(file.getAbsolutePath() + " not found.");
            System.exit(-1);
        } else {
            process(args[0], cl);
        }
    }

    private static void process(String path, CommandLine cl) {
        LOG.info(bundle.getString("processing") + path);
        MPOSeparator mpos = new MPOSeparator();
        int gifDelay = 30;
        if(cl.hasOption("delay")){
            gifDelay = Integer.parseInt(cl.getOptionValue("delay"));
        }
        int stereoImageWidth = 450;
        if(cl.hasOption("width")){
            stereoImageWidth = Integer.parseInt(cl.getOptionValue("width"));
        }
        mpos.separate(new File(path), new FileOutput(path, true), cl.hasOption("si"), cl.hasOption("gif"), gifDelay, true, stereoImageWidth);
    }
}
