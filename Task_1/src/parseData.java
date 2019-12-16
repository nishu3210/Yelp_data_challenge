import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class parseData {
    private String path, data, open_tag, close_tag;
    private Stack<String> active_tag = new Stack<String>();
    private String stack_top;
    private boolean pop_stack;

    private enum STATE {OPEN, CLOSE, END};
    private STATE curr_state, new_doc_flag;

    private static final String START_DOC_TAG = "<DOC>";
    private static final String END_DOC_TAG = "</DOC>";
    private static final String START_DOCNO_TAG = "<DOCNO>";
    private static final String START_HEAD_TAG = "<HEAD>";
    private static final String START_BYLINE_TAG = "<BYLINE>";
    private static final String START_DATELINE_TAG = "<DATELINE>";
    private static final String START_TEXT_TAG = "<TEXT>";


    private String loc;
    public parseData(String a) {
        loc = a;
    }

    public ArrayList<HashMap<String, String>> generateDocuments() throws FileNotFoundException {
        String docno, head, byline, dateline, text;
        ArrayList<HashMap<String, String>> documents = new ArrayList();

        System.out.println("Reading Files.......");
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            if(file.getName().equalsIgnoreCase(".DS_Store"))
                continue;

            new_doc_flag = STATE.END;
            docno = head = byline = dateline = text = "";
            Scanner sc = new Scanner(file);
            pop_stack = false;
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                try {
                    this.parseLine(line);
                    stack_top = active_tag.peek();
                    if(pop_stack) {
                        pop_stack = false;
                    }

                    if(new_doc_flag == STATE.OPEN) {
                        if(stack_top.equalsIgnoreCase(START_DOCNO_TAG))
                            docno = docno.concat(data) + " ";
                        else if(stack_top.equalsIgnoreCase(START_HEAD_TAG))
                            head = head.concat(data) + " ";
                        else if(stack_top.equalsIgnoreCase(START_DATELINE_TAG))
                            dateline = dateline.concat(data) + " ";
                        else if(stack_top.equalsIgnoreCase(START_BYLINE_TAG))
                            byline = byline.concat(data) + " ";
                        else if(stack_top.equalsIgnoreCase(START_TEXT_TAG))
                            text = text.concat(data) + " ";
                    }
                    else if(new_doc_flag == STATE.CLOSE){
                        HashMap<String, String> doc = new HashMap<String, String>();
                        doc.put("DOCNO", docno);
                        doc.put("HEAD", head);
                        doc.put("BYLINE", byline);
                        doc.put("DATELINE", dateline);
                        doc.put("TEXT", text);
                        documents.add(doc);

                        docno = head = byline = dateline = text = "";
                        new_doc_flag = STATE.END;
//						System.out.println("--New Document Added--");
//						System.out.println(doc);
                    }
                }
                catch( EmptyStackException e) {
                    System.out.println("EmptyStack: " + new_doc_flag);
                    if(new_doc_flag == STATE.CLOSE){
                        HashMap<String, String> doc = new HashMap<String, String>();
                        doc.put("DOCNO", docno);
                        doc.put("HEAD", head);
                        doc.put("BYLINE", byline);
                        doc.put("DATELINE", dateline);
                        doc.put("TEXT", text);
                        documents.add(doc);
                        docno = head = byline = dateline = text = "";
                        new_doc_flag = STATE.END;
//						System.out.println("--New Document Added--");
//						System.out.println(doc);
                    }
                }
            }
            sc.close();
        }
        System.out.println("Documents Generated.......");
        return(documents);
    }

    private void parseLine(String line)  {
        data = open_tag = close_tag = "";
        char[] ch = line.toCharArray();
        curr_state = STATE.END;

        // Vulnerability: If two open or close tags are in the same line will not register them as separate tags 
        for(int i = 0; i < line.length(); ++i) {
            switch(ch[i]) {
                case '<':	if(curr_state == STATE.END)
                    curr_state = STATE.OPEN;
                    break;
                case '/':	// This condition is still not completely fool-proof. 
                    if(curr_state == STATE.OPEN)
                        curr_state = STATE.CLOSE;
                    break;
                case '>':	curr_state = STATE.END;
                    break;
                default:	if(curr_state == STATE.END)
                    data = data.concat("" + ch[i]);
                else if(curr_state == STATE.OPEN)
                    open_tag = open_tag.concat("" + ch[i]);
                else if(curr_state == STATE.CLOSE)
                    close_tag = close_tag.concat("" + ch[i]);
            }
        }
        open_tag = "<" + open_tag + ">";
        close_tag = "</" + close_tag + ">";

//		To check if a close tag or open tag is empty simple check for "</>" and "<>" respectively
//		System.out.println(open_tag + "\t" + data + "\t" + close_tag);

        if(open_tag.equalsIgnoreCase(START_DOC_TAG)) {
            active_tag.push(START_DOC_TAG);
            new_doc_flag = STATE.OPEN;
        }
        else if(open_tag.equalsIgnoreCase(START_DOCNO_TAG)) {
            active_tag.push(START_DOCNO_TAG);
        }
        else if(open_tag.equalsIgnoreCase(START_HEAD_TAG)) {
            active_tag.push(START_HEAD_TAG);
        }
        else if(open_tag.equalsIgnoreCase(START_BYLINE_TAG)) {
            active_tag.push(START_BYLINE_TAG);
        }
        else if(open_tag.equalsIgnoreCase(START_DATELINE_TAG)) {
            active_tag.push(START_DATELINE_TAG);
        }
        else if(open_tag.equalsIgnoreCase(START_TEXT_TAG)) {
            active_tag.push(START_TEXT_TAG);
        }
        else if(! open_tag.equalsIgnoreCase("<>")) {
            active_tag.push("NA");
        }

        if(! close_tag.equalsIgnoreCase("</>")) {
            if(close_tag.equalsIgnoreCase(END_DOC_TAG))
                new_doc_flag = STATE.CLOSE;
            // Assumes that open tag and close tag of the diff kinds are always balanced
            pop_stack = true;
        }
        else
            pop_stack = false;
    }

}
