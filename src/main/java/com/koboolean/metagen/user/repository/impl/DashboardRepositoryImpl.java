package com.koboolean.metagen.user.repository.impl;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.dto.BoardViewDto;
import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.board.domain.entity.QBoard;
import com.koboolean.metagen.data.code.domain.entity.QCodeRuleDetail;
import com.koboolean.metagen.data.column.domain.entity.QColumnInfo;
import com.koboolean.metagen.data.dictionary.domain.entity.QStandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.QStandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.QStandardWord;
import com.koboolean.metagen.data.table.domain.entity.QTableInfo;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.home.domain.dto.RecentChangeDto;
import com.koboolean.metagen.system.code.domain.entity.QCodeRule;
import com.koboolean.metagen.system.project.domain.entity.QTemplate;
import com.koboolean.metagen.user.repository.DashboardRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepositoryCustom {

    private final JPAQueryFactory query;

    public List<RecentChangeDto> findRecentChanges(Long projectId, int limit) {
        QStandardWord word = QStandardWord.standardWord;
        QStandardTerm term = QStandardTerm.standardTerm;
        QStandardDomain domain = QStandardDomain.standardDomain;
        QCodeRule rule = QCodeRule.codeRule;
        QCodeRuleDetail codeRuleDetail = QCodeRuleDetail.codeRuleDetail;
        QTemplate template = QTemplate.template;

        List<RecentChangeDto> wordChanges = query
                .select(Projections.constructor(RecentChangeDto.class,
                        Expressions.constant("표준단어"),
                        word.commonStandardWordName,
                        word.updatedBy,
                        word.updated
                ))
                .from(word)
                .where(word.projectId.eq(projectId))
                .orderBy(word.updated.desc())
                .limit(limit)
                .fetch();

        List<RecentChangeDto> termChanges = query
                .select(Projections.constructor(RecentChangeDto.class,
                        Expressions.constant("표준용어"),
                        term.commonStandardTermName,
                        term.updatedBy,
                        term.updated
                ))
                .from(term)
                .where(term.projectId.eq(projectId))
                .orderBy(term.updated.desc())
                .limit(limit)
                .fetch();

        List<RecentChangeDto> domainChanges = query
                .select(Projections.constructor(RecentChangeDto.class,
                        Expressions.constant("표준도메인"),
                        domain.commonStandardDomainName,
                        domain.updatedBy,
                        domain.updated
                ))
                .from(domain)
                .where(domain.projectId.eq(projectId))
                .orderBy(domain.updated.desc())
                .limit(limit)
                .fetch();

        List<RecentChangeDto> codeRuleChanges = query
                .select(Projections.constructor(RecentChangeDto.class,
                        Expressions.constant("코드규칙관리"),
                        rule.codeRuleName,
                        rule.updatedBy,
                        rule.updated
                ))
                .from(rule)
                .where(rule.projectId.eq(projectId))
                .orderBy(rule.updated.desc())
                .limit(limit)
                .fetch();

        List<RecentChangeDto> codeRuleDetailChanges = query
                .select(Projections.constructor(RecentChangeDto.class,
                        Expressions.constant("코드규칙"),
                        codeRuleDetail.methodName,
                        codeRuleDetail.updatedBy,
                        codeRuleDetail.updated
                ))
                .from(codeRuleDetail)
                .where(codeRuleDetail.projectId.eq(projectId))
                .orderBy(codeRuleDetail.updated.desc())
                .limit(limit)
                .fetch();

        List<RecentChangeDto> templateChanges = query
                .select(Projections.constructor(RecentChangeDto.class,
                        Expressions.constant("템플릿"),
                        template.templateName,
                        template.updatedBy,
                        template.updated
                ))
                .from(template)
                .where(template.projectId.eq(projectId))
                .orderBy(template.updated.desc())
                .limit(limit)
                .fetch();

        return Stream.of(wordChanges, termChanges, domainChanges, codeRuleChanges, codeRuleDetailChanges, templateChanges)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(RecentChangeDto::modifiedAt).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public List<BoardViewDto> findNotice(Long projectId, int limit) {
        QBoard board = QBoard.board;

        return query
                .select(Projections.constructor(BoardViewDto.class,
                        board.id,
                        board.projectId,
                        board.createdBy.as("createdBy"),
                        board.created.as("created"),
                        board.title,
                        board.hitCount
                ))
                .from(board)
                .where(board.projectId.eq(projectId))
                .where(board.deleteYn.eq('N'))
                .orderBy(board.created.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public DashboardDto findDataCount(Long projectId) {

        QStandardWord word = QStandardWord.standardWord;
        QStandardTerm term = QStandardTerm.standardTerm;
        QStandardDomain domain = QStandardDomain.standardDomain;

        int wordData = query.selectFrom(word).where(word.projectId.eq(projectId)).where(word.isApproval.eq(false)).fetch().size();
        int termData = query.selectFrom(term).where(term.projectId.eq(projectId)).where(term.isApproval.eq(false)).fetch().size();
        int domainData = query.selectFrom(domain).where(domain.projectId.eq(projectId)).where(domain.isApproval.eq(false)).fetch().size();



        QTableInfo tableInfo = QTableInfo.tableInfo;

        int tableSize = query.selectFrom(tableInfo).where(tableInfo.projectId.eq(projectId)).where(tableInfo.isApproval.eq(false)).fetch().size();

        QColumnInfo columnInfo = QColumnInfo.columnInfo;

        int columnSize = query.selectFrom(columnInfo).where(columnInfo.projectId.eq(projectId)).where(columnInfo.isApproval.eq(false)).fetch().size();

        return DashboardDto.builder()
                .pendingStandardCount(wordData + termData + domainData)
                .pendingTableCount(tableSize)
                .pendingColumnCount(columnSize)
                .build();
    }
}
