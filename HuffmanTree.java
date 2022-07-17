package CompressFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HuffmanTree {
	Map<Character, String> HuffmanCode = new HashMap<>();
	TreeNode root;
	List<TreeNode> list = new ArrayList<>();

	class TreeNode {
		int weight;
		char key;
		TreeNode left, right;

		public TreeNode() {

		}

		public TreeNode(char key, int weight) {
			this.weight = weight;
			this.key = key;
		}
	}

	public HuffmanTree() {
		// TODO Auto-generated constructor stub
	}

	public HuffmanTree(int[] arr) {
	}

	public void sort(int left, int right) {
		// merge sort
		if (left < right) {
			int mid = left + (right - left) / 2;
			sort(left, mid);
			sort(mid + 1, right);
			merge(left, right, mid);
		}
	}

	private void merge(int left, int right, int mid) {
		// TODO Auto-generated method stub
		List<TreeNode> leftList = new ArrayList<>();
		List<TreeNode> rightList = new ArrayList<>();
		int leftLen = mid - left + 1;
		int rightLen = right - mid;
		int leftIndex = 0, rightIndex = 0;
		for (int i = 0; i < leftLen; i++) {
			leftList.add(list.get(i + left));
		}
		for (int i = 0; i < rightLen; i++) {
			rightList.add(list.get(i + mid + 1));
		}

		while (leftIndex < leftList.size() && rightIndex < rightList.size()) {
			if (leftList.get(leftIndex).weight <= rightList.get(rightIndex).weight) {
				list.set(left, leftList.get(leftIndex));
				leftIndex++;
			} else {
				list.set(left, rightList.get(rightIndex));
				rightIndex++;
			}
			left++;
		}
		while (leftIndex < leftList.size()) {
			list.set(left, leftList.get(leftIndex));
			leftIndex++;
			left++;
		}
		while (rightIndex < rightList.size()) {
			list.set(left, rightList.get(rightIndex));
			rightIndex++;
			left++;
		}
	}

	public void createTree() {
		while (list.size() > 1) {
			sort(0, list.size() - 1);
			TreeNode node1 = list.remove(0);
			TreeNode node2 = list.remove(0);
			TreeNode parent = new TreeNode(' ', node1.weight + node2.weight);
			parent.left = node1;
			parent.right = node2;
			list.add(parent);
		}
		root = list.remove(0);
//		printTree();
		createHuffmanCode(root, "");
	}

	public void createHuffmanCode(TreeNode currNode, String code) {
		if (currNode == null) {
			return;
		}
		createHuffmanCode(currNode.left, code + '0');
		if (currNode.left == null && currNode.right == null) {
			System.out.println(currNode.key + ": " + code);
			HuffmanCode.put(currNode.key, code);
		}
		createHuffmanCode(currNode.right, code + '1');
	}


	private void printTree() {
		// TODO Auto-generated method stub
		Deque<TreeNode> queue = new ArrayDeque<>();
		queue.offer(root);
		while (!queue.isEmpty()) {
			int currSize = queue.size();
			for (int i = 0; i < currSize; i++) {
				TreeNode currNode = queue.poll();
				System.out.print(currNode.weight + " ");
				if (currNode.left != null) {
					queue.offer(currNode.left);
				}
				if (currNode.right != null) {
					queue.offer(currNode.right);
				}

			}
			System.out.println();
		}
	}

	public void compressFile(String filePath) {
		Map<Character, Integer> map = new HashMap<>();
		// read file
		File file = new File(filePath);
		String str = "";
		String compressedStrCode = "";
		String compressedStr = "";
		try {
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				str += sc.nextLine();
			}
			// count all characters and put into hashmap

			for (int i = 0; i < str.length(); i++) {
				char currChar = str.charAt(i);
				map.put(currChar, map.getOrDefault(currChar, 0) + 1);
			}
			// for each set create one node
			for (Map.Entry<Character, Integer> entry : map.entrySet()) {
				list.add(new TreeNode(entry.getKey(), entry.getValue()));
			}
			createTree();
			for (int i = 0; i < str.length(); i++) {
				char currChar = str.charAt(i);
				compressedStrCode += HuffmanCode.get(currChar);
			}
//			System.out.println(compressedStrCode);
			// convert to decimal
			int start = 0, end = start + 8;
			File compressedFile = new File("compressedFile.txt");
			FileOutputStream writer = new FileOutputStream(compressedFile);
			try {
				while (end < compressedStrCode.length()) {
					int num = Integer.parseInt(compressedStrCode.substring(start, end), 2);
//					System.out.println(num);
					try {
						// compressedFile.createNewFile();
						writer.write(num);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// compressedStr += (char) (num);
					start = end;
					end = start + 8;
				}
				String lastCompressedStrCode = compressedStrCode.substring(start);
//				System.out.println("lastCompressedStrCode: " + lastCompressedStrCode);
				int addBitCount = 0;
				while (lastCompressedStrCode.length() < 8) {
					lastCompressedStrCode += '0';
					addBitCount++;
				}
//				System.out.println(addBitCount);
//				System.out.println(
//						"Integer.parseInt(lastCompressedStrCode, 2): " + Integer.parseInt(lastCompressedStrCode, 2));
				writer.write(Integer.parseInt(lastCompressedStrCode, 2));
				writer.write(addBitCount);
				// add separator `
				writer.write('`');
				// ****add table****
				for (Map.Entry<Character, String> entry : HuffmanCode.entrySet()) {
					writer.write(entry.getKey());
					int byteNeedRead = entry.getValue().length() / 8 + 1;
					writer.write(byteNeedRead);
					if (byteNeedRead == 0)
						byteNeedRead++;
					int bitsNeedAdd = entry.getValue().length() % 8;
					start = 0;
					end = start + 8;
					while (end < entry.getValue().length()) {
						int num = Integer.parseInt(entry.getValue().substring(start, end), 2);
						writer.write(num);
						start = end;
						end = start + 8;
					}
					String lastByte = entry.getValue().substring(start);
					for (int i = 0; i < bitsNeedAdd; i++) {
						lastByte += '0';
					}
					writer.write(Integer.parseInt(lastByte, 2));
					writer.write(bitsNeedAdd);
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println(compressedStr);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HuffmanTree tree = new HuffmanTree();
		String filePath = "C:\\Users\\imrui\\eclipse-workspace\\Lanjie\\src\\CompressFile\\The_Red_Wheelbarrow.txt";
		tree.compressFile(filePath);
	}
}
