import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * 语法分析器
 */
public class SyntaxAnalyzer {

    private static ArrayList<String> stack = new ArrayList<>(); // 当前栈
    private static ArrayList<Integer> reader = new ArrayList<>(); // 待读队列
    private static Production[] productions = new Production[100]; // 产生式数组
    private static HashMap<Integer, String> map_i2s; // 种别码Map，种别码为键，单词为值
    private static HashMap<String, Integer> map_s2i; // 种别码Map，单词为键，种别码为值

    public static void main(String[] args) {
        int stackTop = 1;
        int readerTop = 0;
        int index = 0; // 当前步骤数
        initMap(); // 初始化种别码Map
        initProductions(); // 产生式初始化
        stack.add(0, String.valueOf(map_s2i.get("$"))); // 在stack底部加上$
        stack.add(stackTop, "program");
        System.out.print("请输入待分析源代码绝对路径: ");

        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.next();
        Sentence fileSentence = new FileSentences(filePath);
        fileSentence.setReader();
        WordAnalysis wordAnalysis = new WordAnalysis(fileSentence);
        wordAnalysis.getToken();
        reader = wordAnalysis.getWordCodeList();

        reader.add(map_s2i.get("$")); // 在reader末尾加上$
        while (stackTop >= 0) {
            System.out.printf("%-6s", "第" + ++index + "步：");
            System.out.printf("%-10s", "当前栈：");
            StringBuffer sb = new StringBuffer(); // 引入StringBuffer仅为控制在控制台的输出格式对齐
            for (int i = 0; i <= stackTop; i++) {
                String str = null;
                try {
                    str = map_i2s.get(Integer.valueOf(stack.get(i)));
                    if (str != null) {
                        sb.append(str + " ");
                    }
                } catch (NumberFormatException e) {
                    sb.append(stack.get(i) + " ");
                }
            }
            System.out.printf("%-30s", sb.toString());
            System.out.print("待读队列：");
            sb = new StringBuffer();
            for (int i = 0; i < reader.size(); i++) {
                sb.append(map_i2s.get(reader.get(i)) + " ");
            }
            System.out.printf("%-55s", sb.toString());

            if (match(stackTop, readerTop)) {
                stackTop--;
                System.out.print("\n");
            } else {
                int i = ll1_table(stackTop, readerTop);
                if (i == -1) {
                    System.out.println("\nError: 该程序在当前栈顶为 " + stack.get(stackTop) + " 处，与待读队列顶处的元素 " + map_i2s.get(reader.get(readerTop)) + " 匹配时发生错误。");
                    return;
                }
                stackTop += stackPush(stackTop, productions[i]); // 压栈
                System.out.printf("%-30s", "下一步所用产生式：" + productions[i].prod);
                System.out.println();
            }
        }
        if (stackTop == -1) {
            System.out.println("语法分析成功");
        }
    }

    private static int stackPush(int stackTop, Production production) {
        int len = production.r_str.length;
        stack.remove(stackTop);
        if ("ε".equals(production.r_str[0])) {
        } else {
            for (int i = len - 1; i >= 0; i--) {
                stack.add(production.r_str[i]);
            }
            return len - 1;
        }
        return -1;
    }

    private static int ll1_table(int stackTop, int readerTop) {
        if ("program".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 0;
            } else {
                return -1;
            }
        } else if ("block".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 1;
            } else {
                return -1;
            }
        } else if ("decls".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("}".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("basic".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("if".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("while".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("do".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("break".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 2;
            } else {
                return -1;
            }
        } else if ("decls'".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else if ("}".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else if ("basic".equals(map_i2s.get(reader.get(readerTop)))) {
                return 4;
            } else if ("if".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else if ("while".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else if ("do".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else if ("break".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 3;
            } else {
                return -1;
            }
        } else if ("decl".equals(stack.get(stackTop))) {
            if ("basic".equals(map_i2s.get(reader.get(readerTop)))) {
                return 5;
            } else {
                return -1;
            }
        } else if ("type".equals(stack.get(stackTop))) {
            if ("basic".equals(map_i2s.get(reader.get(readerTop)))) {
                return 6;
            } else {
                return -1;
            }
        } else if ("type'".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 7;
            } else if ("[".equals(map_i2s.get(reader.get(readerTop)))) {
                return 8;
            } else {
                return -1;
            }
        } else if ("stmts".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else if ("}".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else if ("if".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else if ("while".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else if ("do".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else if ("break".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else {
                return -1;
            }
        } else if ("stmts'".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 10;
            } else if ("}".equals(map_i2s.get(reader.get(readerTop)))) {
                return 11;
            } else if ("if".equals(map_i2s.get(reader.get(readerTop)))) {
                return 10;
            } else if ("while".equals(map_i2s.get(reader.get(readerTop)))) {
                return 10;
            } else if ("do".equals(map_i2s.get(reader.get(readerTop)))) {
                return 10;
            } else if ("break".equals(map_i2s.get(reader.get(readerTop)))) {
                return 10;
            } else if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 10;
            } else {
                return -1;
            }
        } else if ("stmt".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 12;
            } else if ("if".equals(map_i2s.get(reader.get(readerTop)))) {
                return 13;
            } else if ("while".equals(map_i2s.get(reader.get(readerTop)))) {
                return 14;
            } else if ("do".equals(map_i2s.get(reader.get(readerTop)))) {
                return 15;
            } else if ("break".equals(map_i2s.get(reader.get(readerTop)))) {
                return 16;
            } else if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 17;
            } else if ("}".equals(map_i2s.get(reader.get(readerTop)))) {
                return 9;
            } else {
                return -1;
            }
        } else if ("stmt'".equals(stack.get(stackTop))) {
            if ("{".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("}".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("if".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("while".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("do".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("break".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 18;
            } else if ("else".equals(map_i2s.get(reader.get(readerTop)))) {
                return 19;
            } else {
                return -1;
            }
        } else if ("loc".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 55;
            } else {
                return -1;
            }
        } else if ("loc'".equals(stack.get(stackTop))) {
            if ("[".equals(map_i2s.get(reader.get(readerTop)))) {
                return 20;
            } else if ("*".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("/".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("+".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("&&".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("!=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("==".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("<".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if ("<=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if (">".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if (">=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 21;
            } else {
                return -1;
            }
        } else if ("bool".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 22;
            } else {
                return -1;
            }
        } else if ("bool'".equals(stack.get(stackTop))) {
            if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 23;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 24;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 23;
            } else {
                return -1;
            }
        } else if ("join".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 25;
            } else {
                return -1;
            }
        } else if ("join'".equals(stack.get(stackTop))) {
            if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 26;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 26;
            } else if ("&&".equals(map_i2s.get(reader.get(readerTop)))) {
                return 27;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 26;
            } else {
                return -1;
            }
        } else if ("equality".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 28;
            } else {
                return -1;
            }
        } else if ("equality'".equals(stack.get(stackTop))) {
            if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 30;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 29;
            } else if ("&&".equals(map_i2s.get(reader.get(readerTop)))) {
                return 29;
            } else if ("!=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 30;
            } else if ("==".equals(map_i2s.get(reader.get(readerTop)))) {
                return 31;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 29;
            } else {
                return -1;
            }
        } else if ("rel".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 32;
            } else {
                return -1;
            }
        } else if ("rel'".equals(stack.get(stackTop))) {
            if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 33;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 33;
            } else if ("&&".equals(map_i2s.get(reader.get(readerTop)))) {
                return 33;
            } else if ("!=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 33;
            } else if ("==".equals(map_i2s.get(reader.get(readerTop)))) {
                return 33;
            } else if ("<".equals(map_i2s.get(reader.get(readerTop)))) {
                return 34;
            } else if ("<=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 35;
            } else if (">".equals(map_i2s.get(reader.get(readerTop)))) {
                return 36;
            } else if (">=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 37;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 33;
            } else {
                return -1;
            }
        } else if ("expr".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 38;
            } else {
                return -1;
            }
        } else if ("expr'".equals(stack.get(stackTop))) {
            if ("+".equals(map_i2s.get(reader.get(readerTop)))) {
                return 40;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 41;
            } else if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if ("&&".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if ("!=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if ("==".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if ("<".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if ("<=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if (">".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if (">=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 48;
            } else {
                return -1;
            }
        } else if ("term".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 39;
            } else {
                return -1;
            }
        } else if ("term'".equals(stack.get(stackTop))) {
            if ("*".equals(map_i2s.get(reader.get(readerTop)))) {
                return 42;
            } else if ("/".equals(map_i2s.get(reader.get(readerTop)))) {
                return 43;
            } else if ("+".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if (")".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("||".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("&&".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("!=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("==".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("<".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if ("<=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if (">".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if (">=".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else if (";".equals(map_i2s.get(reader.get(readerTop)))) {
                return 44;
            } else {
                return -1;
            }
        } else if ("unary".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 45;
            } else if ("!".equals(map_i2s.get(reader.get(readerTop)))) {
                return 46;
            } else if ("-".equals(map_i2s.get(reader.get(readerTop)))) {
                return 47;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 45;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 45;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 45;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 45;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 45;
            } else {
                return -1;
            }
        } else if ("factor".equals(stack.get(stackTop))) {
            if ("id".equals(map_i2s.get(reader.get(readerTop)))) {
                return 49;
            } else if ("(".equals(map_i2s.get(reader.get(readerTop)))) {
                return 50;
            } else if ("num".equals(map_i2s.get(reader.get(readerTop)))) {
                return 51;
            } else if ("real".equals(map_i2s.get(reader.get(readerTop)))) {
                return 52;
            } else if ("true".equals(map_i2s.get(reader.get(readerTop)))) {
                return 53;
            } else if ("false".equals(map_i2s.get(reader.get(readerTop)))) {
                return 54;
            } else {
                return -1;
            }
        } else {
            System.out.println("语法错误");
        }
        return -1;
    }

    private static boolean match(int stackTop, int readerTop) {
        try {
            int stackTopVal = Integer.valueOf(stack.get(stackTop)); // 未抛出异常说明是终结符
            if (stackTopVal == reader.get(0)) {

                stack.remove(stackTop);
                reader.remove(readerTop);
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            // 抛出异常说明是非终结符
            return false;
        }
    }


    private static void initProductions() {
        productions[0] = new Production("program",
                new String[]{"block"},
                "program --> block");
        productions[1] = new Production("block",
                new String[]{String.valueOf(map_s2i.get("{")), "decls", "stmts", String.valueOf(map_s2i.get("}"))},
                "block --> { decls stmts }");
        productions[2] = new Production("decls",
                new String[]{"decls'"},
                "decls --> decls'");
        productions[3] = new Production("decls'",
                new String[]{"ε"},
                "decls' --> ε");
        productions[4] = new Production("decls'",
                new String[]{"decl", "decls'"},
                "decls' --> decl decls'");
        productions[5] = new Production("decl",
                new String[]{String.valueOf(map_s2i.get("type")), String.valueOf(map_s2i.get("id")), String.valueOf(map_s2i.get(";"))},
                "decl --> type id;");
        productions[6] = new Production("type",
                new String[]{String.valueOf(map_s2i.get("basic")), "type'"},
                "type --> basic type'");
        productions[7] = new Production("type'",
                new String[]{"ε"},
                "type' --> ε");
        productions[8] = new Production("type'",
                new String[]{String.valueOf(map_s2i.get("[")), String.valueOf(map_s2i.get("num")), String.valueOf(map_s2i.get("]")), "type'"},
                "type' --> [num]type'");
        productions[9] = new Production("stmts",
                new String[]{"stmts'"},
                "stmts --> stmts'");
        productions[10] = new Production("stmts'",
                new String[]{"stmt", "stmts"},
                "stmts' --> stmt stmts'");
        productions[11] = new Production("stmts'",
                new String[]{"ε"},
                "stmts' --> ε");
        productions[12] = new Production("stmt",
                new String[]{"block"},
                "stmt --> block");
        productions[13] = new Production("stmt",
                new String[]{String.valueOf(map_s2i.get("if")), String.valueOf(map_s2i.get("(")), "bool", String.valueOf(map_s2i.get(")")), "stmt"},
                "stmt --> if(bool)stmt");
        productions[14] = new Production("stmt",
                new String[]{String.valueOf(map_s2i.get("while")), String.valueOf(map_s2i.get("(")), "bool", String.valueOf(map_s2i.get(")")), "stmt"},
                "stmt --> while(bool)stmt");
        productions[15] = new Production("stmt",
                new String[]{"do", "stmt", String.valueOf(map_s2i.get("while")), String.valueOf(map_s2i.get("(")), "bool", String.valueOf(map_s2i.get(")"))},
                "stmt --> do stmt while(bool);");
        productions[16] = new Production("stmt",
                new String[]{"break", String.valueOf(map_s2i.get(";"))},
                "stmt --> break;");
        productions[17] = new Production("stmt",
                new String[]{"loc", String.valueOf(map_s2i.get("=")), "bool", String.valueOf(map_s2i.get(";"))},
                "stmt --> loc=bool;");
        productions[18] = new Production("stmt'",
                new String[]{"ε"},
                "stmt' --> ε");
        productions[19] = new Production("stmt'",
                new String[]{String.valueOf(map_s2i.get("else")), "stmt"},
                "stmt' --> else stmt");
        productions[20] = new Production("loc'",
                new String[]{String.valueOf(map_s2i.get("[")), "num", String.valueOf(map_s2i.get("]")), "loc'"},
                "loc' --> [num] loc'");
        productions[21] = new Production("loc'",
                new String[]{"ε"},
                "loc' --> ε");
        productions[22] = new Production("bool",
                new String[]{"join", "bool"},
                "bool --> join bool'");
        productions[23] = new Production("bool'",
                new String[]{"ε"},
                "bool' --> ε");
        productions[24] = new Production("bool'",
                new String[]{String.valueOf(map_s2i.get("||")), "join", "bool'"},
                "bool' --> || join bool'");
        productions[25] = new Production("join",
                new String[]{"equality", "join'"},
                "join --> equality join'");
        productions[26] = new Production("join'",
                new String[]{"ε"},
                "join' --> ε");
        productions[27] = new Production("join'",
                new String[]{String.valueOf(map_s2i.get("&&")), "equality", "join'"},
                "join' --> && equality join'");
        productions[28] = new Production("equality",
                new String[]{"rel", "equality'"},
                "equality --> rel equality'");
        productions[29] = new Production("equality'",
                new String[]{"ε"},
                "equality' --> ε");
        productions[30] = new Production("equality'",
                new String[]{String.valueOf(map_s2i.get("!=")), "rel" ,"equality'"},
                "equality' --> != rel equality'");
        productions[31] = new Production("equality'",
                new String[]{String.valueOf(map_s2i.get("==")), "rel", "equality'"},
                "equality' --> == rel equality'");
        productions[32] = new Production("rel",
                new String[]{"expr", "rel'"},
                "rel --> expr rel'");
        productions[33] = new Production("rel'",
                new String[]{"ε"},
                "rel' --> ε");
        productions[34] = new Production("rel'",
                new String[]{String.valueOf(map_s2i.get("<")), "expr"},
                "rel' --> < expr");
        productions[35] = new Production("rel'",
                new String[]{String.valueOf(map_s2i.get("<=")), "expr"},
                "rel' --> <= expr");
        productions[36] = new Production("rel'",
                new String[]{String.valueOf(map_s2i.get(">")), "expr"},
                "rel' --> > expr");
        productions[37] = new Production("rel'",
                new String[]{String.valueOf(map_s2i.get(">=")), "expr"},
                "rel' --> >= expr");
        productions[38] = new Production("expr",
                new String[]{"term", "expr'"},
                "expr --> term expr'");
        productions[39] = new Production("term",
                new String[]{"unary", "term'"},
                "term --> unary term'");
        productions[40] = new Production("expr'",
                new String[]{String.valueOf(map_s2i.get("+")), "term", "expr'"},
                "expr' --> + term expr'");
        productions[41] = new Production("expr'",
                new String[]{String.valueOf(map_s2i.get("-")), "term", "expr'"},
                "expr' --> - term expr'");
        productions[42] = new Production("term'",
                new String[]{String.valueOf(map_s2i.get("*")), "unary", "term'"},
                "term' --> * unary term'");
        productions[43] = new Production("term'",
                new String[]{String.valueOf(map_s2i.get("/")), "unary", "term'"},
                "term' --> / unary term'");
        productions[44] = new Production("term'",
                new String[]{"ε"},
                "term' --> ε");
        productions[45] = new Production("unary",
                new String[]{"factor"},
                "unary --> factor");
        productions[46] = new Production("unary",
                new String[]{String.valueOf(map_s2i.get("!")), "unary"},
                "unary --> ！unary ");
        productions[47] = new Production("unary",
                new String[]{String.valueOf(map_s2i.get("-")), "unary"},
                "unary --> - unary ");
        productions[48] = new Production("expr'",
                new String[]{"ε"},
                "expr' --> ε");
        productions[49] = new Production("factor",
                new String[]{"loc"},
                "factor --> loc");
        productions[50] = new Production("factor",
                new String[]{String.valueOf(map_s2i.get("(")), "bool" , String.valueOf(map_s2i.get(")"))},
                "factor --> (bool) ");
        productions[51] = new Production("factor",
                new String[]{String.valueOf(map_s2i.get("num"))},
                "factor --> num");
        productions[52] = new Production("factor",
                new String[]{"real"},
                "factor --> real");
        productions[53] = new Production("factor",
                new String[]{String.valueOf(map_s2i.get("true")},
                "factor --> true ");
        productions[54] = new Production("factor",
                new String[]{String.valueOf(map_s2i.get("false")},
                "factor --> false");
        productions[55] = new Production("loc",
                new String[]{String.valueOf(map_s2i.get("id")), String.valueOf(map_s2i.get("[")), "num", String.valueOf(map_s2i.get("]")), "loc'"},
                "loc --> [num] loc'");

    }

    private static void initMap() {
        map_s2i = new HashMap<>();
        map_s2i.put("id", 0);
        map_s2i.put("(", 10);
        map_s2i.put(")", 11);
        map_s2i.put("[", 12);
        map_s2i.put("]", 13);
        map_s2i.put("{", 14);
        map_s2i.put("}", 15);
        map_s2i.put("\"", 16);
        map_s2i.put("'", 17);
        map_s2i.put(",", 20);
        map_s2i.put(";", 21);
        map_s2i.put("<", 30);
        map_s2i.put("<=", 31);
        map_s2i.put(">", 32);
        map_s2i.put(">=", 33);
        map_s2i.put("==", 34);
        map_s2i.put("!=", 35);
        map_s2i.put("+", 40);
        map_s2i.put("-", 41);
        map_s2i.put("*", 42);
        map_s2i.put("/", 43);
        map_s2i.put("||", 50);
        map_s2i.put("&&", 51);
        map_s2i.put("!", 52);
        map_s2i.put("=", 60);
        map_s2i.put("num", 61);
        map_s2i.put("real", 62);
        map_s2i.put("if", 81);
        map_s2i.put("else", 82);
        map_s2i.put("while", 83);
        map_s2i.put("do", 84);
        map_s2i.put("break", 85);
        map_s2i.put("true", 86);
        map_s2i.put("false", 87);
        map_s2i.put("basic", 88);
        map_s2i.put("$", 90);

        map_i2s = new HashMap<>();
        map_i2s.put(0, "id");
        map_i2s.put(10, "(");
        map_i2s.put(11, ")");
        map_i2s.put(12, "[");
        map_i2s.put(13, "]");
        map_i2s.put(14, "{");
        map_i2s.put(15, "}");
        map_i2s.put(16, "\"");
        map_i2s.put(17, "'");
        map_i2s.put(20, ",");
        map_i2s.put(21, ";");
        map_i2s.put(30, "<");
        map_i2s.put(31, "<=");
        map_i2s.put(32, ">");
        map_i2s.put(33, ">=");
        map_i2s.put(34, "==");
        map_i2s.put(35, "!=");
        map_i2s.put(40, "+");
        map_i2s.put(41, "-");
        map_i2s.put(42, "*");
        map_i2s.put(43, "/");
        map_i2s.put(50, "||");
        map_i2s.put(51, "&&");
        map_i2s.put(52, "!");
        map_i2s.put(60, "=");
        map_i2s.put(61, "num");
        map_i2s.put(62, "real");
        map_i2s.put(81, "if");
        map_i2s.put(82, "else");
        map_i2s.put(83, "while");
        map_i2s.put(84, "do");
        map_i2s.put(85, "break");
        map_i2s.put(86, "true");
        map_i2s.put(87, "false");
        map_i2s.put(88, "basic");
        map_i2s.put(90, "$");
    }

    /**
     * 产生式类
     */
    private static class Production {
        String l_str;
        String[] r_str;
        String prod;
        public Production(String l_str, String[] r_str, String prod) {
            this.l_str = l_str;
            this.r_str = r_str;
            this.prod = prod;
        }
    }
}
