package ru.rozvezev;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Application for grouping strings.
 */
public class GroupingApplication {

    public static void main(String[] args) {
        try (FileWriter fileWriter = new FileWriter("src/main/resources/output.txt", false);
             Stream<String> streamFromFile = Files.lines(Paths.get("src/main/resources/lng.txt"))) {

            ///// Подготовка данных /////

            long startTime = System.currentTimeMillis();
            List<String[]> ListOfElementsArrays = streamFromFile.map(str -> str.split(";"))
                    .map(arr -> Arrays.stream(arr)
                            .map(str -> str.substring(1, str.length() - 1))
                            .collect(Collectors.joining(";")))
                    .filter(str -> !str.contains("\""))                                 //<-- удаление неправильных строк
                    .map(str -> str.split(";"))
                    .collect(Collectors.toList());



            ///// Разбиение на группы /////

            // Map<Номер группы, Список строк (в виде масива элементов строки) в данной группе>
            Map<Integer, List<String[]>> groups = new HashMap<>();

            // List - набор колонок; каждая колонка - Map<Элемент строки в данной колонке, Номер группы элемента>>
            List<Map<String, Integer>> columns = new ArrayList<>();

            Integer groupNumber = null;
            boolean groupFound;
            int groupsSize;
            for (String[] elementsArray : ListOfElementsArrays) {
                groupFound = false;
                groupsSize = groups.size();

                for (int i = 0; i < elementsArray.length; i++) {
                    String element = elementsArray[i];

                    if (columns.size() == i)
                        columns.add(new HashMap<>());

                    if (element.isEmpty()) continue;

                    Map<String, Integer> column = columns.get(i);
                    groupNumber = column.get(element);
                    if (Objects.nonNull(groupNumber)) {
                        groups.get(groupNumber).add(elementsArray);
                        groupFound = true;
                        break;
                    } else {
                        column.put(element, groupsSize);
                    }
                }

                /*Если groupFound=true, то мы нашли подходящую группу данной строке, и нам нужно снова пробежаться по
                всем элеметам данной строки и переназначить или назначить им номер найденной группы. Если groupFound=true
                добавляем новую группу в список.*/
                if (groupFound) {
                    for (int i = 0; i < elementsArray.length; i++) {
                        String word = elementsArray[i];
                        if (columns.size() == i)
                            columns.add(new HashMap<>());

                        if (word.isEmpty()) continue;

                        columns.get(i).put(word, groupNumber);
                    }

                } else {
                    groups.put(groupsSize, new ArrayList<>(List.<String[]>of(elementsArray)));
                }
            }


            
            ///// Вывод в файл /////

            fileWriter.write("Число групп с более чем одним элементом: "
                    + groups.values().stream().filter(list -> list.size() > 1).count() + "\n\n");

            AtomicInteger counter = new AtomicInteger(1);
            groups.values().stream()
                    .sorted((list1, list2) -> list2.size() - list1.size())
                    .map(list -> list.stream()
                            .map(arr -> String.join(", ", arr))
                            .distinct()                                                 //<-- удаление дупликотов в группах
                            .collect(Collectors.toList()))
                    .forEachOrdered(group -> {
                        try {
                            fileWriter.write("Группа " + counter.getAndIncrement() + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        group.forEach(str -> {
                            try {
                                fileWriter.write(str + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    });

            fileWriter.flush();

            long endTime = System.currentTimeMillis();
            System.out.println("Время выполнения (сек): " + ((endTime - startTime) / 1000.0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
