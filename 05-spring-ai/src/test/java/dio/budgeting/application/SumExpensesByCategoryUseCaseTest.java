package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SumExpensesByCategoryUseCaseTest {

    // Repositório de mentira: guarda as transações numa lista, sem banco de dados
    static class FakeTransactionRepository implements TransactionRepository {
        private final List<Transaction> transactions = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            transactions.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAllByCategory(Category category) {
            return transactions.stream()
                    .filter(transaction -> transaction.getCategory() == category)
                    .toList();
        }
    }

    @Test
    void somaOsGastosDaCategoria() {
        var repository = new FakeTransactionRepository();
        repository.save(new Transaction("Compra no mercado", 3000, Category.GROCERIES));
        repository.save(new Transaction("Outra compra no mercado", 2000, Category.GROCERIES));
        repository.save(new Transaction("Farmácia", 1500, Category.PHARMA));

        var useCase = new SumExpensesByCategoryUseCase(repository);

        long total = useCase.execute(Category.GROCERIES);

        assertEquals(5000, total);
    }

    @Test
    void retornaZeroQuandoNaoHaGastos() {
        var repository = new FakeTransactionRepository();
        repository.save(new Transaction("Farmácia", 1500, Category.PHARMA));

        var useCase = new SumExpensesByCategoryUseCase(repository);

        long total = useCase.execute(Category.GROCERIES);

        assertEquals(0, total);
    }

}