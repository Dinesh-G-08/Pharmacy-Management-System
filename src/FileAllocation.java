import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.util.List;

public class FileAllocation extends JFrame {
    private JTextField fileNameField, fileSizeField, startBlockField, endBlockField;
    private JComboBox<String> methodBox;
    private JTextArea resultArea;
    private JPanel blockPanel;
    private final int TOTAL_BLOCKS = 104;
    private final int BLOCK_SIZE_MB = 50;
    private final JButton[] blocks = new JButton[TOTAL_BLOCKS];
    private final Map<String, List<Integer>> fileAllocationMap = new HashMap<>();
    private final boolean[] memory = new boolean[TOTAL_BLOCKS];

    public FileAllocation() {
        setTitle("File Allocation Simulator");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initTopPanel();
        initBlockPanel();
        initBottomPanel();
        setVisible(true);
    }

    private void initTopPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        fileNameField = new JTextField(10);
        fileSizeField = new JTextField(5);
        startBlockField = new JTextField(5);
        endBlockField = new JTextField(5);
        methodBox = new JComboBox<>(new String[]{"Contiguous", "Linked", "Indexed"});
        JButton allocateBtn = new JButton("Allocate");
        JButton deallocateBtn = new JButton("Deallocate");
        JButton resetBtn = new JButton("Reset");

        allocateBtn.addActionListener(e -> allocateFile());
        deallocateBtn.addActionListener(e -> deallocateFile());
        resetBtn.addActionListener(e -> resetAll());

        panel.add(new JLabel("File Name:"));
        panel.add(fileNameField);
        panel.add(new JLabel("Size (MB):"));
        panel.add(fileSizeField);
        panel.add(new JLabel("Start Block:"));
        panel.add(startBlockField);
        panel.add(new JLabel("End Block:"));
        panel.add(endBlockField);
        panel.add(methodBox);
        panel.add(allocateBtn);
        panel.add(deallocateBtn);
        panel.add(resetBtn);
        add(panel, BorderLayout.NORTH);
    }

    private void initBlockPanel() {
        blockPanel = new JPanel(new GridLayout(13, 8, 2, 2));
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            blocks[i] = new JButton(String.valueOf(i));
            blocks[i].setEnabled(false);
            blocks[i].setBackground(Color.WHITE);
            blockPanel.add(blocks[i]);
        }
        add(blockPanel, BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        resultArea = new JTextArea(5, 60);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void allocateFile() {
        String name = fileNameField.getText().trim();
        int sizeInMB;

        if (name.isEmpty()) {
            showError("File name cannot be empty.");
            return;
        }

        try {
            sizeInMB = Integer.parseInt(fileSizeField.getText().trim());
            if (sizeInMB <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Enter a valid positive integer for size in MB.");
            return;
        }

        if (fileAllocationMap.containsKey(name)) {
            showError("File already exists.");
            return;
        }

        // Convert MB to required number of blocks
        int requiredBlocks = (int) Math.ceil((double) sizeInMB / BLOCK_SIZE_MB);

        String method = (String) methodBox.getSelectedItem();
        boolean success = false;

        switch (method) {
            case "Contiguous":
                success = allocateContiguous(name, requiredBlocks);
                break;
            case "Linked":
                success = allocateLinked(name, requiredBlocks);
                break;
            case "Indexed":
                success = allocateIndexed(name, requiredBlocks);
                break;
        }

        if (!success) {
            showError("Allocation failed. Not enough space or invalid range.");
        }
    }
 private boolean allocateContiguous(String name, int size) {
        Integer startBlock = null;
        if (!startBlockField.getText().trim().isEmpty()) {
                    try {
                        startBlock = Integer.valueOf(startBlockField.getText().trim());
                        if (startBlock < 0 || startBlock >= TOTAL_BLOCKS) {
                            showError("Starting block out of range.");
                        }
                    } catch (NumberFormatException e) {
                        showError("Invalid starting block number.");
                    }
                }
        if (startBlock != null) {
            if (startBlock + size > TOTAL_BLOCKS) return false;

            for (int j = 0; j < size; j++) {
                if (memory[startBlock + j]) return false;
            }

            List<Integer> allocated = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                memory[startBlock + j] = true;
                allocated.add(startBlock + j);
                blocks[startBlock + j].setBackground(Color.GREEN);
            }
            fileAllocationMap.put(name, allocated);
            resultArea.append(name + " allocated at blocks (Contiguous): " + allocated +
                    " [Start: " + allocated.get(0) + ", End: " + allocated.get(allocated.size() - 1) + "]\n");
            return true;
        } else {
            for (int i = 0; i <= TOTAL_BLOCKS - size; i++) {
                boolean found = true;
                for (int j = 0; j < size; j++) {
                    if (memory[i + j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    List<Integer> allocated = new ArrayList<>();
                    for (int j = 0; j < size; j++) {
                        memory[i + j] = true;
                        allocated.add(i + j);
                        blocks[i + j].setBackground(Color.GREEN);
                    }
                    fileAllocationMap.put(name, allocated);
                    resultArea.append(name + " allocated at blocks: " + allocated +
                            " [Start: " + allocated.get(0) + ", End: " + allocated.get(allocated.size() - 1) + "]\n");
                    return true;
                }
            }
            return false;
        }
    }


    private boolean allocateLinked(String name, int size) {
        try {
            int startBlock = Integer.parseInt(startBlockField.getText().trim());
            int endBlock = Integer.parseInt(endBlockField.getText().trim());

            if (startBlock < 0 || endBlock >= TOTAL_BLOCKS || startBlock > endBlock) {
                showError("Invalid start or end block range.");
                return false;
            }

            List<Integer> available = new ArrayList<>();
            for (int i = startBlock; i <= endBlock; i++) {
                if (!memory[i]) {
                    available.add(i);
                }
            }

            if (available.size() < size) {
                return false;
            }

            List<Integer> allocated = available.subList(0, size);
            for (int block : allocated) {
                memory[block] = true;
                blocks[block].setBackground(Color.ORANGE);
            }

            fileAllocationMap.put(name, new ArrayList<>(allocated));
            resultArea.append(name + " allocated (Linked) at blocks: " + allocated + "\n");
            return true;

        } catch (NumberFormatException e) {
            showError("Invalid block numbers.");
            return false;
        }
    }

    private boolean allocateIndexed(String name, int size) {
        try {
            int startBlock = Integer.parseInt(startBlockField.getText().trim());
            int endBlock = Integer.parseInt(endBlockField.getText().trim());

            if (startBlock < 0 || endBlock >= TOTAL_BLOCKS || startBlock > endBlock) {
                showError("Invalid start or end block range.");
                return false;
            }

            List<Integer> available = new ArrayList<>();
            for (int i = startBlock; i <= endBlock; i++) {
                if (!memory[i]) {
                    available.add(i);
                }
            }

            if (available.size() < size + 1) {
                return false;
            }

            int indexBlock = available.get(0);
            List<Integer> allocated = new ArrayList<>();
            allocated.add(indexBlock);
            memory[indexBlock] = true;
            blocks[indexBlock].setBackground(Color.MAGENTA);

            for (int i = 1; i <= size; i++) {
                int dataBlock = available.get(i);
                memory[dataBlock] = true;
                allocated.add(dataBlock);
                blocks[dataBlock].setBackground(Color.CYAN);
            }

            fileAllocationMap.put(name, allocated);
            resultArea.append(name + " allocated (Indexed). Index: " + indexBlock +
                    ", Data blocks: " + allocated.subList(1, allocated.size()) + "\n");
            return true;

        } catch (NumberFormatException e) {
            showError("Invalid block numbers.");
            return false;
        }
    }

    private void deallocateFile() {
        String name = fileNameField.getText().trim();
        if (!fileAllocationMap.containsKey(name)) {
            showError("File not found.");
            return;
        }

        for (int idx : fileAllocationMap.get(name)) {
            memory[idx] = false;
            blocks[idx].setBackground(Color.WHITE);
        }

        fileAllocationMap.remove(name);
        resultArea.append("Deallocated file: " + name + "\n");
    }

    private void resetAll() {
        Arrays.fill(memory, false);
        fileAllocationMap.clear();
        for (JButton btn : blocks) {
            btn.setBackground(Color.WHITE);
        }
        resultArea.setText("");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileAllocation::new);
    }
}
