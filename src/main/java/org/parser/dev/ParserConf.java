package org.parser.dev;

import com.google.gson.Gson;
import org.parser.data.Ticket;
import org.parser.data.Tickets;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ParserConf {
    DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd.MM.yyHH:mm");
    Pattern pattern=Pattern.compile("\\d{2}:\\d{2}");
    HashMap<String,Integer> mapOfMinTimeTravel=new HashMap<>();
    List<Integer> listOfPrice=new ArrayList<>();
    public ParserConf() throws IOException {

        Tickets tickets=new Gson().fromJson(new FileReader("../tickets.json"),Tickets.class);
        for(Ticket oneTicket: tickets.getTickets()){
            if(oneTicket.getOrigin().equals("VVO") && oneTicket.getDestination().equals("TLV")) {
                findMinTimeTravelByCompany(oneTicket);
                listOfPrice.add(oneTicket.getPrice());
            }
        }
        OutAnswer();

    }
    private void OutAnswer(){
       System.out.println("Минимальное время полета между городами Владивосток и Тель-Авив для каждого авиаперевозчика:");
       for(HashMap.Entry<String,Integer> oneCompany : mapOfMinTimeTravel.entrySet()){
           int hours= oneCompany.getValue()/60;
           int minutes= oneCompany.getValue()%60;
           System.out.println("Компания: "+oneCompany.getKey()+" Минимальная время полета: "+hours+" ч. "+minutes+" мин.");
       }
       System.out.println("Разницу между средней ценой  и медианой для полета между городами  Владивосток и Тель-Авив = "+findDifAverageAndMedianPrice());
    }
    private int findDifAverageAndMedianPrice(){
        return Math.abs(findAverage()-findMedian());
    }
    private int findMedian(){
        Collections.sort(listOfPrice);
        int median;
        if (listOfPrice.size()%2==0){
            median= listOfPrice.get(listOfPrice.size()/2)+listOfPrice.get(listOfPrice.size()/2-1);
        }else{
            median= listOfPrice.get(listOfPrice.size()/2);
        }
        return median;
    }
    private int findAverage(){
        return listOfPrice.stream().mapToInt(Integer::intValue).sum()/listOfPrice.size();
    }
    private void findMinTimeTravelByCompany(Ticket ticket){
        LocalDateTime departureDateTime;
        LocalDateTime arrivalDateTime;
        int minDifference;
        if(!isValid(ticket.getDeparture_time())) {
            ticket.setDeparture_time("0"+ticket.getDeparture_time());
        }
        departureDateTime= convertStrIntoLocalDateTime(ticket.getDeparture_date() + ticket.getDeparture_time());

        if(!isValid(ticket.getArrival_time())){
            ticket.setArrival_time("0"+ticket.getArrival_time());
        }
        arrivalDateTime=convertStrIntoLocalDateTime(ticket.getArrival_date()+ticket.getArrival_time());

        minDifference= (int) Math.abs(departureDateTime.until(arrivalDateTime, ChronoUnit.MINUTES));
        if(!mapOfMinTimeTravel.containsKey(ticket.getCarrier()) || mapOfMinTimeTravel.get(ticket.getCarrier())>minDifference){
            mapOfMinTimeTravel.put(ticket.getCarrier(),minDifference);
        }
    }
    private LocalDateTime convertStrIntoLocalDateTime(String stringToConvert){
        return LocalDateTime.parse(stringToConvert,formatter);
    }
    private boolean isValid(String stingToConvert){
        return  pattern.matcher(stingToConvert).find();
    }
}
