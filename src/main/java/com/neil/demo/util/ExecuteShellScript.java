package com.neil.demo.util;

import java.io.*;

public class ExecuteShellScript {

    public static void main(String[] args) {
    /*    try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh",
                    "checkOriginMergeCode.sh billservice  feature/dh/202403 false 日常合并 dev");
            processBuilder.directory(new File("E:/workspace/handday-all"));
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("Shell脚本执行完毕，退出码为：" + exitCode);

            // 获取Shell脚本输出结果
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
*/
       /* try {
            // 构建命令和参数
            ProcessBuilder processBuilder = new ProcessBuilder("sh","echo -e \"Shell\"","wait");
            // 执行Shell脚本
            Process process = processBuilder.start();
            processBuilder.redirectErrorStream(true);
            // 读取Shell脚本的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 等待Shell脚本执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Shell脚本执行成功");
                System.out.println("输出结果:");
                System.out.println(output.toString());
            } else {
                System.out.println("Shell脚本执行失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
/*


        try {
            String[] envVars = {"PATH=E:/workspace/handday-all"};
            String command = "./checkOriginMergeCode.sh billservice  feature/dh/202403 false 日常合并 dev";
            Process process = Runtime.getRuntime().exec(command,envVars);
            int exitValue = process.waitFor();

            String line = null;
            InputStreamReader stdISR = null;
            InputStreamReader errISR = null;
            stdISR = new InputStreamReader(process.getInputStream());
            BufferedReader stdBR = new BufferedReader(stdISR);
            while ((line = stdBR.readLine()) != null) {
                System.out.println("STD line:" + line);
            }

            errISR = new InputStreamReader(process.getErrorStream());
            BufferedReader errBR = new BufferedReader(errISR);
            while ((line = errBR.readLine()) != null) {
                System.out.println("ERR line:" + line);
            }
            System.out.println("Shell脚本执行结果：" + exitValue);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
*/

        try {
            String gitBashPath = "D:\\Program Files\\Git\\bin\\bash.exe"; // 指定 Git Bash 所在路径
            String command = gitBashPath + " -c 'E:/workspace/handday-all/checkOriginMergeCode.sh billservice  feature/dh/202402 false 日常合并 pre'";
            Process process = Runtime.getRuntime().exec(command);
            // 读取Shell脚本的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output.append(line).append("\n");
            }

            // 等待Shell脚本执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Shell脚本执行成功");
                System.out.println("输出结果:");
                System.out.println(output.toString());
            } else {
                System.out.println("Shell脚本执行失败");
            }
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
