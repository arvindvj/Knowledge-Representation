package ai3;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Knowledge_Representation {
 
//	Global Variables
 	static long start = 0;
	static long curr = 0;

	static int kb_count;
	static int track;
	
	static String[] queries;
	
	static HashSet<String> variables = new HashSet<>();
	static HashSet<String> constants = new HashSet<>();
	
	static ArrayList<String> temp = new ArrayList<>();
	
	static ArrayList<ArrayList<String>> preds = new ArrayList<>();
	
	static HashMap<String,String> map = new HashMap<>();
	
//	Main Function
	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(new FileReader("input.txt"));
		
		int query_count = sc.nextInt();
		queries = new String[query_count];
 		
		for(int i = 0; i < query_count; i++) {
			queries[i] = sc.nextLine();
			queries[i] = queries[i].replaceAll("\\s+","");
			if(queries[i].length()==0) {
				i--;
				continue;
			}
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(queries[i]);
			while(m.find()) {
				String[] temp = m.group(1).split(",");
				for(int j = 0; j < temp.length; j++) {
					if(!constants.contains(temp[j])) {
						constants.add(temp[j]);
					}
				}
			}
		}
		
		kb_count = sc.nextInt();
		String[] kb = new String[kb_count];
		
		for(int i = 0; i < kb_count; i++) {
			kb[i] = sc.nextLine();
			kb[i] = kb[i].replaceAll("\\s+","");
			
			if(kb[i].length()==0) {
				i--;
				continue;
			}
			
			ArrayList<String> list = new ArrayList<String>(Arrays.asList(kb[i].split("\\|")));
			preds.add(list);
			
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(kb[i]);
			while(m.find()) {
				String[] temp = m.group(1).split(",");
				for(int j = 0; j < temp.length; j++) {
					char character = temp[j].charAt(0);
					int ascii = (int) character;
					if(ascii > 96 && temp[j].length() == 1) {
						if(!variables.contains(temp[j])) {
							variables.add(temp[j]);
						}
					}
						
					else {
						if(!constants.contains(temp[j])) {
							constants.add(temp[j]);
						}
					}
				}
			}
		}
		
		ArrayList<ArrayList<String>> new_kb = new ArrayList<>((ArrayList<ArrayList<String>>)preds.clone());
		BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
     
     	start = System.currentTimeMillis();
		for(int i = 0; i < query_count; i++) {
			ArrayList<String> query = new ArrayList<>();
			query.add(negator(queries[i]));
			new_kb.add((ArrayList<String>)query.clone());
			boolean answer = check(query,new_kb);
			if(answer) {
             	writer.write("TRUE");
				writer.newLine();
            }
			else {
				writer.write("FALSE");
				writer.newLine();
			}
			new_kb.clear();
			new_kb.addAll((ArrayList<ArrayList<String>>)preds.clone());
		}
		
     	writer.close();
		sc.close();
	}

//	Negate Predicate
	private static String negator(String string) {
		String s;
		if(string.charAt(0) == '~') {
			s = string.substring(1);
		}
		else {
			s = '~'+string;
		}
		return s;
	}

//	Query Selector
	private static boolean check(ArrayList<String> query, ArrayList<ArrayList<String>> new_kb) {
		for(int i = 0; i < query.size(); i++) {
			if(search(query.get(i),new_kb,query))
				return true;
		}
		return false;
	}

//	Search Knowledge Base
	private static boolean search(String string, ArrayList<ArrayList<String>> new_kb, ArrayList<String> query) {
		ArrayList<String> q = new ArrayList<>((ArrayList<String>)query.clone());
		q.remove(string);
		String s = negator(string).split("\\(")[0] + "(";
		
		for(int i = 0; i < new_kb.size(); i++) {
			INNER: for(int j = 0; j < new_kb.get(i).size(); j++) {
				
             	curr = (System.currentTimeMillis()-start)/1000; 
				if(curr > 14.8) {
					return false;
				}
				s = negator(string).split("\\(")[0] + "(";
				
				if(new_kb.get(i).get(j).contains(s) && s.charAt(0) == new_kb.get(i).get(j).charAt(0)) {
					ArrayList<String> result = new ArrayList<>(new_kb.get(i));
					if(query.size()!=0)
						result.addAll((ArrayList<String>)q.clone());
					String joined = String.join("|", result);
					
					Matcher qm = Pattern.compile("\\(([^)]+)\\)").matcher(string);
					Matcher sm = Pattern.compile("\\(([^)]+)\\)").matcher(joined);
					Matcher tm = Pattern.compile("\\(([^)]+)\\)").matcher(new_kb.get(i).get(j));
					
					String[] qs = null;
					String[] ts = null;
					
					while(qm.find()) {
						qs = qm.group(1).split(",");
					}
					ArrayList<ArrayList<String>> as = new ArrayList<>();
					while(sm.find()) {
						ArrayList<String> list = new ArrayList<String>(Arrays.asList(sm.group(1).split(",")));
						as.add(list);
					}
					while(tm.find()) {
						ts = tm.group(1).split(",");
					}
					
					HashMap<String,String> vars = new HashMap<>();
					for(int k = 0; k < qs.length; k++) {
						if(constants.contains(qs[k])) {
							if(constants.contains(ts[k])) {
								if(qs[k].equals(ts[k])) {
									
								}
								else {
									continue INNER;
								}
							}
							else {
								vars.put(ts[k], qs[k]);
							}
						}
						else {
							if(constants.contains(ts[k])) {
								vars.put(qs[k], ts[k]);
							}
						}
					}
					
					String[] ss = new String[as.size()];
					for (Map.Entry<String, String> var : vars.entrySet()) {
						for(int k = 0; k < as.size(); k++) {
							for(int l = 0; l < as.get(k).size(); l++) {
								if(as.get(k).get(l).equals(var.getKey())) {
									as.get(k).set(l, var.getValue());
								}
							}
							ss[k] = String.join(",", as.get(k));
						}
					}
					
					for(int k = 0; k < result.size(); k++) {
						if(ss[k] == null) {
							continue;
						}
						
						result.set(k, result.get(k).replaceAll("\\([^\\(]*\\)", ""));
						result.set(k, (result.get(k)+"("+ss[k]+")").trim());						
					}
					
					StringBuilder sb = new StringBuilder();
					for(int k = 0; k < qs.length; k++) {
						if(vars.containsKey(qs[k])) {
							qs[k] = vars.get(qs[k]);
						}
						sb.append(qs[k]);
						if(k == qs.length-1) {
							sb.append(")");
						}
						else {
							sb.append(",");
						}
					}
					
					s = s + sb.toString();
					result.remove(s);
					
					if(result.size() == 0)
						return true;
					
					if(new_kb.contains(result)) {
						s = negator(string).split("\\(")[0] + "(";
						continue INNER;
					}
					
					new_kb.add((ArrayList<String>)result.clone());
					
					if(check(result, new_kb))
							return true;
				}
			}
		}
		return false;
	}
}
