
package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class SumExpensesByCategoryUseCase {
    private final TransactionRepository transactionRepository;

    public SumExpensesByCategoryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "sum-expenses-by-category", description = "Soma os gastos financeiros por categoria em centavos")
    public long execute(@ToolParam(description = "Categoria de uma transação") Category category) {
        return transactionRepository.findAllByCategory(category).stream()
                .mapToLong(transaction -> transaction.getAmount())
                .sum();
    }
}
