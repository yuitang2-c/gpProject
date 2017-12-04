
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * AST10106 Introduction to Programming - Group Programming Project
 * Group No.: 45
 *
 * @author TANG Kai Yui, MOK Chun Tung, CHAN Sui Shan, KAYNAT Zainab
 * 
 */
public class Scheduler {

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String eventName;
    private int eventLength;
    private boolean exHoliday;
    private boolean limitTime;

    // A candidate period of interest
    private Date periodStart;
    private Date periodEnd;
    
    private int intStartTimeSlot;
    private int intEndTimeSlot;
    private int intCurrentTimeSlot;
    private int intEventLength;
    private int intRange;
    private int score[];
    private int notAttend[];
    
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventLength(int eventLength) {
        this.eventLength = eventLength;
    }

    public void setLimitTime(boolean limitTime) {
        this.limitTime = limitTime;
    }

    public void setExHoliday(boolean exHoliday) {
        this.exHoliday = exHoliday;
    }

    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setTimeRange(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void preprocessInput() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

        String sd = startDate.format(dateFormatter);
        String ed = endDate.format(dateFormatter);

        String st = "000000";
        String et = "000000";
        StringBuilder temp = new StringBuilder();
        // processing starttime
        temp.append(sd);
        temp.append(st);
        System.out.println(temp);
        intStartTimeSlot = timeConverter(temp);
        temp.delete(0, temp.length());
        
        temp.append(ed);
        temp.append(et);        
        System.out.println(temp);
        intEndTimeSlot = timeConverter(temp);
        temp.delete(0, temp.length());
        intRange = intEndTimeSlot - intStartTimeSlot;
        System.out.println(intRange);
        score = new int[intRange];
        notAttend = new int[intRange];
        for (int i = 0; i < intRange; i++) {
            score [i] = 0;
            notAttend [i] = 0;
        }
        
        if (limitTime) {
            st = startTime.format(timeFormatter);
            et = endTime.format(timeFormatter);
            temp.append(st);
            System.out.println(temp);
            int time;
            time = temp.codePointAt(3) - 48;
            time += (temp.codePointAt(2) - 48) * 10;
            time += (temp.codePointAt(1) - 49) * 60;
            time += (temp.codePointAt(0) - 48) * 600;
            System.out.println(time);
            for (int i = 0; i < intRange; i += 1440) {
                for (int j = 0; j < time; j++) {
                    score [i + j] = -2147483648;
                }
            }
            temp.delete(0, 6);
            temp.append(et);
            System.out.println(temp);
            time = temp.codePointAt(3) - 48;
            time += (temp.codePointAt(2) - 48) * 10;
            time += (temp.codePointAt(1) - 48) * 60;
            time += (temp.codePointAt(0) - 48) * 600;
            System.out.println(time);
            for (int i = 0; i < intRange; i += 1440) {
                for (int j = time; j < 1440; j++) {
                    score [i + j] = -2147483648;
                }
            }
        }
        
        try {
            periodStart = new DateTime(sd + "T" + st);
            periodEnd = new DateTime(ed + "T" + et);
            System.out.println("period start: " + periodStart + ", period end: " + periodEnd);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    
    public void start() {
        preprocessInput();
        File[] files = new File("data").listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".ics")) {
                printCalendarFile(file.getPath());
            }
        }
        int maxScore = 0, bestDate = 0;
        for (int i = 1; i <= intRange; i++){
            if (score[i - 1] > maxScore){
                bestDate = intStartTimeSlot + i - 1;
                maxScore = score[i];
            }
        }
        System.out.printf("%d", bestDate / 365);
    }

    public static Date readDateProperty(Property property) throws ParseException {
        String value = property.getValue();
        Parameter param = property.getParameter(Parameter.VALUE);
        Date dt;
        if (param != null && param.getValue().equals("DATE")) {
            dt = new DateTime(value + "T000000");
        } else {
            dt = new DateTime(value);
        }
        /*System.out.println(dt);
        System.out.println(value);*/
        return dt;
    }
    

    public static int timeConverter(StringBuilder time) {
        // adding seconds
        int total = (time.codePointAt(10) - 48) * 10 + time.codePointAt(11) - 48;
        //System.out.println(total);
        
        //adding hours
        total += ((time.codePointAt(8) - 48) * 10 + time.codePointAt(9) - 48) * 60;
        //System.out.println(total);
        
        //adding days
        total += ((time.codePointAt(6) - 48) * 10 + time.codePointAt(7) - 48 - 1) * 24 * 60;
        //System.out.println(total);
        
        //determine leap year or not and adding year;
        int year = (time.codePointAt(0) - 48) * 1000;
        year += (time.codePointAt(1) - 48) * 100;
        year += (time.codePointAt(2) - 48) * 10;
        year += time.codePointAt(3) - 48;
        for (int i = 1; i < year; i++){
            if ((i % 400 == 0) || (i % 100 != 0 && i % 4 == 0)) total += 366 * 24 * 60;
            else                                                total += 365 * 24 * 60;
        }
        
        // adding months
        int month = (time.codePointAt(4) - 48) * 10 + time.codePointAt(5) - 48;
        if ((year % 400 == 0) || (year % 100 != 0 && year % 4 == 0)){
            int leapYearMonth[] = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30};
            for (int i = 1; i < month; i++){
                //System.out.printf("%d %d\n", i, leapYearMonth[i - 1]);
                total += leapYearMonth[i - 1] * 24 * 60;
            }
        }
        else {
            int yearMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30};
            for (int i = 1; i < month; i++){
                //System.out.printf("%d %d\n", i, yearMonth[i - 1]);
                total += yearMonth[i - 1]  * 24 * 60;
            }
        }
        //System.out.println(total);
        return total;
    }
    
    public void printCalendarFile(String filePath) {
        try {
            FileInputStream fin = new FileInputStream(filePath);
            System.out.println("\nProcessing " + filePath + " ... ");

            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(fin);

            String calName = calendar.getProperty("X-WR-CALNAME").getValue();
            String studentInfo = calendar.getProperty("X-WR-CALDESC").getValue();
            String[] studentData = studentInfo.split("\n");
            
            for (String s : studentData) {
                System.out.println(s);
            }
            
            // The current time slot to check against the event
            // Reply: We are using another method to chek against the event

            
            //Our edited code

            for (Iterator<CalendarComponent> i = calendar.getComponents().iterator(); i.hasNext();) {
                Component component = (Component) i.next();
                
                if (!component.getName().equals(Component.VEVENT)) {
                    continue;
                }

                Property summary = component.getProperty(Property.SUMMARY);
                System.out.println("<Event>: " + summary.getValue());

                Property description = component.getProperty(Property.DESCRIPTION);
                if (description != null) {
                    System.out.println("\tDescription: " + description.getValue());
                }

                Property dtStart = component.getProperty(Property.DTSTART);
                Date evtStart = readDateProperty(dtStart);
                System.out.println("\tEvent Start: " + evtStart);

                Property dtEnd = component.getProperty(Property.DTEND);
                Date evtEnd = readDateProperty(dtEnd);
                System.out.println("\tEvent End:   " + evtEnd);

                
                System.out.println(summary.getValue() + eventName);
                if (!summary.getValue().equals(eventName)){
                    
                    if (exHoliday) {
                        if (calName.equals("Hong Kong Public Holidays")){
                            StringBuilder temp = new StringBuilder();
                            temp.append(evtStart);
                            temp.delete(8, 9);
                            //System.out.println(temp);
                            intCurrentTimeSlot = timeConverter(temp) - intStartTimeSlot;
                            if (intCurrentTimeSlot > 0 && intCurrentTimeSlot < intRange)
                            for (int j = 0; j < 1440; j++){
                                score [intCurrentTimeSlot + j] = -2147483648;
                            }
                        }
                    }
                    
                    Property rRule = component.getProperty(Property.RRULE);
                    if (rRule != null) {
                        // partially reform
                        RRule r = new RRule(rRule.getValue());
                        Date seed = evtStart;
                        DateList list = r.getRecur().getDates(seed, periodStart, periodEnd, Value.DATE_TIME);
                        /*if (0 == list.size()) {
                            System.out.println("\tThe event does not occur in the specified period.");
                        } else {
                            System.out.println("\tThe event occurs on the dates below in the specified period.");
                        }*/
                        for (int n = 0; n < list.size(); n++) {
                            DateTime rDateStart = (DateTime) list.get(n);
                            DateTime rDateEnd = new DateTime(rDateStart.toString().substring(0, 8) + "T" +
                                evtEnd.toString().substring(9, 15));
                            StringBuilder temp = new StringBuilder();
                            temp.append(rDateStart);
                            temp.delete(8,9);
                            //System.out.println(temp);
                            int tempStart = timeConverter(temp);
                            System.out.println(tempStart);
                            temp.delete(0,14);
                            temp.append(rDateEnd);
                            //System.out.println(temp);
                            int tempEnd = timeConverter(temp);
                            System.out.println(tempEnd);
                            if (tempStart > 0 && tempEnd < intRange) {
                                for (int j = tempStart; j < tempEnd; j++) {
                                    notAttend [j]++;
                                }
                            }
                            /*System.out.print("\t" + rDateStart + " compares with " + slotStart + ": " + 
                                rDateStart.compareTo(slotStart));
                            System.out.println(", " + rDateEnd + " compares with " + slotEnd + ": " + 
                                rDateEnd.compareTo(slotEnd));*/
                        }
                    }else {
                        // partially reform
                        /*System.out.println("\t" + evtStart + " compares with " + slotStart + ": " + 
                            evtStart.compareTo(slotStart));
                        System.out.println("\t" + evtEnd + " compares with " + slotEnd + ": " + 
                            evtEnd.compareTo(slotEnd));*/
                            StringBuilder temp = new StringBuilder();
                            temp.append(evtStart);
                            temp.delete(8,9);
                            int tempStart = timeConverter(temp);
                            System.out.println(tempStart);
                            int length = temp.length();
                            temp.delete(0,length);
                            temp.append(evtEnd);
                            temp.delete(8,9);
                            //System.out.println(temp);
                            int tempEnd = timeConverter(temp);
                            System.out.println(tempEnd);
                            if (tempStart > 0 && tempEnd < intRange) {
                                for (int j = tempStart; j < tempEnd; j++) {
                                    notAttend [j]++;
                                }
                            }
                    }
                }
                
                
                else {
                    System.out.println("wtf?");
                    Property rRule = component.getProperty(Property.RRULE);
                    if (rRule != null) {
                        // partially reform
                        RRule r = new RRule(rRule.getValue());
                        Date seed = evtStart;
                        DateList list = r.getRecur().getDates(seed, periodStart, periodEnd, Value.DATE_TIME);
                        if (0 == list.size()) {
                            System.out.println("\tThe event does not occur in the specified period.");
                        } else {
                            System.out.println("\tThe event occurs on the dates below in the specified period.");
                        }
                        for (int n = 0; n < list.size(); n++) {
                            DateTime rDateStart = (DateTime) list.get(n);
                            DateTime rDateEnd = new DateTime(rDateStart.toString().substring(0, 8) + "T" + 
                                evtEnd.toString().substring(9, 15));
                            StringBuilder temp = new StringBuilder();
                            temp.append(rDateStart);
                            temp.delete(8,9);
                            System.out.println(temp);
                            int tempStart = timeConverter(temp) - intStartTimeSlot;
                            System.out.println(tempStart);
                            int length = temp.length();          
                            temp.delete(0,length);
                            temp.append(rDateEnd);
                            System.out.println(temp);
                            int tempEnd = timeConverter(temp) - intStartTimeSlot;
                            System.out.println(tempEnd);
                            if (tempStart > 0 && tempEnd < intRange) {
                                String choice = description.getValue();
                                System.out.println(choice);
                                if (choice.equals("1st preference")){
                                    for (int j = 0; j < tempEnd - tempStart - eventLength * 60; j++){
                                        score [tempStart + j] += 5;
                                    }
                                }
                                if (choice.equals("2nd preference")){
                                    for (int j = 0; j < tempEnd - tempStart - eventLength * 60; j++){
                                        score [tempStart + j] += 3;
                                    }
                                }
                                if (choice.equals("3rd preference")){
                                    for (int j = 0; j < tempEnd - tempStart - eventLength * 60; j++){
                                        score [tempStart + j] += 2;
                                    }
                                }                                
                            }
                            /*System.out.print("\t" + rDateStart + " compares with " + slotStart + ": " + 
                                rDateStart.compareTo(slotStart));
                            System.out.println(", " + rDateEnd + " compares with " + slotEnd + ": " + 
                                rDateEnd.compareTo(slotEnd));*/
                        }
                    } else {
                        // partially reform
                        /*System.out.println("\t" + evtStart + " compares with " + slotStart + ": " + 
                            evtStart.compareTo(slotStart));
                        System.out.println("\t" + evtEnd + " compares with " + slotEnd + ": " + 
                            evtEnd.compareTo(slotEnd));*/
                            StringBuilder temp = new StringBuilder();
                            temp.append(evtStart);
                            temp.delete(8,9);
                            System.out.println(temp);
                            int tempStart = timeConverter(temp) - intStartTimeSlot;
                            System.out.println(tempStart);
                            
                            int length = temp.length();          
                            temp.delete(0,length);
                            temp.append(evtEnd);
                            temp.delete(8,9);
                            System.out.println(temp);
                            int tempEnd = timeConverter(temp) - intStartTimeSlot;
                            System.out.println(tempEnd);
                            if (tempStart > 0 && tempEnd < intRange) {
                                String choice = description.getValue();
                                System.out.println(choice);
                                if (choice.equals("1st preference")){
                                    for (int j = 0; j < tempEnd - tempStart - eventLength * 60; j++){
                                        score [tempStart + j] += 5;
                                    }
                                }
                                if (choice.equals("2nd preference")){
                                    for (int j = 0; j < tempEnd - tempStart - eventLength * 60 ; j++){
                                        score [tempStart + j] += 3;
                                    }
                                }
                                if (choice.equals("3rd preference")){
                                    for (int j = 0; j < tempEnd - tempStart - eventLength * 60; j++){
                                        score [tempStart + j] += 2;
                                    }
                                }                                
                            }
                    }
                }
            }
        } catch (IOException | ParseException | ParserException ex) {
            ex.printStackTrace();
        }
    }
}
