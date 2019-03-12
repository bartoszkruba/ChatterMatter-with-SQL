package server.serverApp;

import models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


public class AdminSystemMonitoring implements Runnable {
    private Scanner scan;
    private final long startMillis;
    private LinkedBlockingDeque<String> systemLogs;
    private ServerApp serverApp;
    private boolean isRunning;

    public AdminSystemMonitoring(ServerApp serverApp) {
        scan = new Scanner(System.in);
        startMillis = Calendar.getInstance().getTimeInMillis();
        systemLogs = new LinkedBlockingDeque<>();
        this.serverApp = serverApp;
        this.isRunning=true;
    }


    @Override
    public void run() {
        while (isRunning) {
            Scanner scan = new Scanner(System.in);
            String adminInput = scan.nextLine();
            switch (adminInput.toLowerCase()) {
                case "connectedusers":
                    System.out.println("All connected users: " + ActiveUserController.getInstance().getUsers().size());
                    break;

                case "usersinchannel":
                    usersInChannel();
                    break;
                case "channelamount":
                    System.out.println(ActiveChannelController.getInstance().getAmountOfChannels());
                    break;

                case "channellist":
                    channelList();
                    break;

                case "userlist":
                    ActiveUserController.getInstance().printUsers();

                    break;

                case "printlog":
                    printLog();
                    break;

                case "uptime":
                    long upTimeInSeconds = (Calendar.getInstance().getTimeInMillis() - startMillis);
                    String upTime = getDurationBreakdown(upTimeInSeconds);
                    System.out.println(upTime);
                    break;

                case "kill":
                    kill();
                    break;

                case "help":
                    helpCommands();
                    break;

                default:
                    System.out.println("Faulty input. Type 'help' for command list");
            }

        }
    }

    private void kill(){
        this.isRunning=false;
        serverApp.kill();
    }

    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        String sb = String.valueOf(days) +
                " Days " +
                hours +
                " Hours " +
                minutes +
                " Minutes " +
                seconds +
                " Seconds";
        return (sb);
    }

    private void usersInChannel() {
        try {
            System.out.println("Specify which channel");
            String whatChannel = scan.nextLine();
            System.out.println(ActiveChannelController.getInstance().getChannel(whatChannel).getUsers().size());
            System.out.println("Get user id and nickname for users in THIS channel? y/n");
            if (scan.nextLine().equals("y")) {
                ActiveChannelController.getInstance().getChannel(whatChannel).getUsers()
                        .stream()
                        .sorted(Comparator.comparing(User::getNickName))
                        .forEach(user -> {
                            System.out.println(user.getNickName() + " " + user.getID());
                        });
            }
        } catch (NullPointerException e) {
            System.out.println("Non existing channel entered");
        }
    }

    private void channelList() {
        Arrays.stream(ActiveChannelController.getInstance().getChannelList()).forEach(System.out::println);
    }

    private String getTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "[" + now.format(formatter) + "] ";
    }

    public void addToLog(String log) {
        this.systemLogs.add(getTimeStamp() + log);
    }

    private void printLog(){
        this.systemLogs.stream().forEach(System.out::println);
    }

    private void helpCommands() {
        String[] array = new String[8];
        array[0] = "connectedusers: Display the total amount of users on the server";
        array[1] = "usersinchannel: Display the total amount of users in the specified channel";
        array[2] = "channelamount: Display the total amount of channels on the server";
        array[3] = "channellist: Display a list of all channels";
        array[4] = "userlist: Display ALL users nickname and unique id";
        array[5] = "uptime: Display the servers uptime";
        array[6] = "kill: Shutdown server";
        array[7] = "printlog: Prints server log";
        Arrays.stream(array).forEach(System.out::println);
    }
}


