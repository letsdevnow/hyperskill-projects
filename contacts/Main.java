package contacts;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static contacts.Main.println;

public class Main {

    public static void main(String[] args) {
        PhoneBook phoneBook = args.length > 0 ? new PhoneBook(args[0]) : new PhoneBook();
        phoneBook.runMainMenu();
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void println(int n) {
        System.out.println(n);
    }
}


class PhoneBook {
    private List<Contact> contactsDB = new ArrayList<>();
    private final Scanner sc = new Scanner(System.in);
    private final String fileName;

    PhoneBook() {
        fileName = null;
    }

    PhoneBook(String fileName) {
        this.fileName = fileName;
        deserialize();
    }

    public void runMainMenu() {
        boolean toExit = false;

        while (!toExit) {
            println("[menu] Enter action (add, list, search, count, exit): ");
            String command = sc.nextLine().trim();
            switch (command) {
                case "add":
                    addContact();
                    break;
                case "list":
                    listOfContacts();
                    break;
                case "search":
                    search();
                    break;
                case "count":
                    System.out.printf("The Phone Book has %d records.\n", countOfContacts());
                    break;
                case "exit":
                    toExit = true;
                    break;
                default:
                    println("Please enter an appropriate command (add, list, search, count, exit): ");
            }
            println("");
        }

    }

    public void runRecordMenu(int index) {
        println("\n[record] Enter action (edit, delete, menu): ");
        String command = sc.nextLine().trim();
        switch (command) {
            case "edit":
                editContact(index);
                break;
            case "delete":
                removeContact(index);
                break;
            case "menu":
                return;
            default:
                println("No such command");
        }
        println("");
        runRecordMenu(index);
    }

    public void addContact() {
        println("Enter the type (person, organization):");
        String contactType = sc.nextLine().trim();

        Contact contact;
        if (contactType.equals("person")) {
            contact =  new Person();
        } else if (contactType.equals("organization")) {
            contact =  new Organization();
        } else {
            println("No such contact type");
            return;
        }

        String[] fields = contact.getFields();
        for (int i = 0; i < fields.length; i++) {
            println("Enter the " + fields[i] + ":");
            contact.setField(fields[i], sc.nextLine().trim());
        }

        contactsDB.add(contact);
        if (fileName != null) {
            serialize();
        }
        println("The record added.");
        println("");
    }

    public void removeContact(int index) {
        if (index >= 0 && index < countOfContacts()) {
            contactsDB.remove(index);
            println("The record removed!");
            if (fileName != null) {
                serialize();
            }
        }
    }

    public void editContact(int index) {
        String[] fields = contactsDB.get(index).getFields();
        StringBuilder fieldsStr = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            fieldsStr.append(i == fields.length - 1 ? fields[i] : fields[i] + ", ");
        }

        println("Select a field (" + fieldsStr + "):");
        String command = sc.nextLine().trim();
        if (Arrays.asList(fields).contains(command)) {
            println("Enter " + command + ":");
            contactsDB.get(index).setField(command, sc.nextLine().trim());
            contactsDB.get(index).setEdited();
            if (fileName != null) {
                serialize();
            }
            println("Saved");
            println(contactsDB.get(index).toStringFull());
        }

    }

    public int countOfContacts() {
        return contactsDB.size();
    }

    public void printInfo() {
        if (countOfContacts() == 0) {
            println("No records in the Phone book");
        } else {
            listOfContacts();
            println("Select a record:");
            if (sc.hasNextInt()) {
                int index = Integer.parseInt(sc.nextLine()) - 1;
                if (index >= 0 && index < countOfContacts()) {
                    println(contactsDB.get(index).toStringFull());
                }
            }
        }
        println("");
    }

    public void listOfContacts() {
        for (int i = 0; i < contactsDB.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, contactsDB.get(i).toString());
        }
        println("\n[list] Enter action ([number], back): ");
        if (sc.hasNextInt()) {
            int index = Integer.parseInt(sc.nextLine()) - 1;
            println(contactsDB.get(index).toStringFull());
            runRecordMenu(index);
        }
    }

    public void search() {
        println("Enter search query:");
        String query = sc.nextLine();
        Pattern pattern = Pattern.compile(".*" + query + ".*", Pattern.CASE_INSENSITIVE);

        List<Integer> foundIndexes = new ArrayList<>();
        for (int i = 0; i < contactsDB.size(); i++) {
            if (pattern.matcher(contactsDB.get(i).stringForSearch()).matches()) {
                foundIndexes.add(i);
            }
        }
        println("Found " + foundIndexes.size() + " results");
        for (int i = 0; i < foundIndexes.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, contactsDB.get(foundIndexes.get(i)).toString());
        }

        println("[search] Enter action ([number], back, again): ");
        if (sc.hasNextInt()) {
            int index = Integer.parseInt(sc.nextLine()) - 1;
            println(contactsDB.get(foundIndexes.get(index)).toStringFull());
            runRecordMenu(foundIndexes.get(index));
        } else {
            String command = sc.nextLine().trim();
            switch (command) {
                case "back":
                    break;
                case "again":
                    search();
                    break;
                default:
            }
        }

    }

    public void serialize() {
        try (
                FileOutputStream fos = new FileOutputStream(fileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
        ) {
            oos.writeObject(contactsDB);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deserialize() {
        try (
                FileInputStream fis = new FileInputStream(fileName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
        ) {
            contactsDB = (List<Contact>) ois.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

abstract class Contact implements Serializable {
    protected String name;
    protected String phone;
    protected LocalDateTime created;
    protected LocalDateTime edited;

    public Contact() {
        created = LocalDateTime.now();
    }
    protected void setName(String name) {
        this.name = name;
    }

    protected String getName() {
        return name;
    }

    protected void setPhone(String phone) {
        if (phone.equals("")) {
            this.phone = "[no number]";
        }
        else if (isPhoneValid(phone)) {
            this.phone = phone;
        } else {
            println("Wrong number format!");
            this.phone = "[no number]";
        }
    }

    protected String getPhone() {
        return phone;
    }

    protected boolean hasNumber() {
        return phone != null;
    }

    protected boolean isPhoneValid(String phone) {
        String regex1 = "\\+?\\s?" + "\\(?\\w+\\)?" + "[\\w\\s-]*";
        String regex2 = "\\+?" + "[\\w\\s-]+" + "\\(?\\w{2,}\\)?" + "[\\w\\s-]*";
        Pattern pattern1 = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE);

        Pattern pattern3 = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern3.matcher(phone);
        boolean noOneNumberGroup = true;
        int groupsNumber = 0;
        while (matcher.find()) {
            groupsNumber++;
            if (matcher.end() - matcher.start() < 2 && groupsNumber > 1) {
                noOneNumberGroup = false;
            }
        }

        return (pattern1.matcher(phone).matches() || pattern2.matcher(phone).matches()) && noOneNumberGroup;
    }

    protected LocalDateTime getCreated() {
        return created;
    }

    protected void setEdited() {
        this.edited = LocalDateTime.now();
    }

    protected LocalDateTime getEdited() {
        return edited;
    }

    protected abstract String toStringFull();

    protected abstract String stringForSearch();

    protected abstract String[] getFields();

    protected abstract void setField(String fieldName, String value);

}


class Person extends Contact {
    private String surname;
    private LocalDate birthDate;
    private Gender gender;

    public Person() {
        super();
    }

    public Person(String name, String surname, String phone) {
        super();
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSurname() {
        return surname;
    }

    public void setBirthDate(String birthDate) {
        try {
            this.birthDate = LocalDate.parse(birthDate);
        } catch (DateTimeException e) {
            println("Bad birth date!");
        }
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setGender(String gender) {
        if (gender.equalsIgnoreCase("M")) {
            this.gender = Gender.MALE;
        } else if (gender.equalsIgnoreCase("F")) {
            this.gender = Gender.FEMALE;
        } else {
            println("Bad gender!");
        }
    }

    public Gender getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return name + " " + surname;
    }

    @Override
    protected String toStringFull() {
        return "Name: " + name + "\n" +
                "Surname: " + surname + "\n" +
                "Birth date: " + (birthDate == null ? "[no data]" : birthDate) + "\n" +
                "Gender: " + (gender == null ? "[no data]" : gender) + "\n" +
                "Number: " + (phone == null ? "[no data]" : phone) + "\n" +
                "Time created: " + created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                "Time last edit: " + (edited == null ? "[no data]" : edited.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

    }

    protected String stringForSearch() {
        return name + " " + surname + " " + birthDate + " " + phone;
    }

    public String[] getFields() {
        return new String[]{"name", "surname", "birth", "gender", "number"};
    }

    public void setField(String fieldName, String value) {
        switch (fieldName) {
            case "name":
                this.setName(value);
                break;
            case "surname":
                this.setSurname(value);
                break;
            case "birth":
                this.setBirthDate(value);
                break;
            case "gender":
                this.setGender(value);
                break;
            case "number":
                this.setPhone(value);
                break;
        }
    }
}

class Organization extends Contact {
    private String address;

    public Organization() {
        super();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String toStringFull() {
        return "Organization name: " + name + "\n" +
                "Address: " + address + "\n" +
                "Number: " + (phone == null ? "[no data]" : phone) + "\n" +
                "Time created: " + created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                "Time last edit: " + (edited == null ? "[no data]" : edited.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

    }

    protected String stringForSearch() {
        return name + " " + address + " " + phone;
    }

    public String[] getFields() {
        return new String[]{"name", "address", "number"};
    }

    public void setField(String fieldName, String value) {
        switch (fieldName) {
            case "name":
                this.setName(value);
                break;
            case "address":
                this.setAddress(value);
                break;
            case "number":
                this.setPhone(value);
                break;
        }
    }

}

enum Gender {
    MALE, FEMALE;
}
