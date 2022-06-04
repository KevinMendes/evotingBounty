(function() {
    const _hasProp = {}.hasOwnProperty;

    const _extends = function(child, parent) {
        for (const key in parent) {
            if (_hasProp.call(parent, key)) {
                child[key] = parent[key];
            }
        }

        function Ctor() {
            this.constructor = child;
        }

        Ctor.prototype = parent.prototype;
        child.prototype = new Ctor();
        child.__super__ = parent.prototype;

        return child;
    };

    const log = function(msg) {
        // Empty function
    };

    const BallotItem = (function() {
        function BallotItem(_atId) {
            this.id = _atId;
            this.title = '';
            this.description = '';
        }

        BallotItem.prototype.getQualifiedId = function() {
            if (this.parent) {
                return this.parent.getQualifiedId() + '_' + this.id;
            } else {
                return this.id;
            }
        };

        return BallotItem;

    })();

    const Option = (function(_super) {
        _extends(Option, _super);

        function Option(rawData, isBlank) {
            this.id = rawData.id;
            this.attribute = rawData.attribute;
            this.chosen = false;
            this.isBlank = isBlank || false;
            this.ordinal = 0;
            this.prime = rawData.representation;
            Option.__super__.constructor.call(this, rawData.id);
        }

        return Option;

    })(BallotItem);

    const Question = (function(_super) {
        _extends(Question, _super);

        function Question(rawData) {
            this.options = [];
            this.id = rawData.id;
            this.attribute = rawData.attribute;
            this.optionsMinChoices = rawData.min;
            this.optionsMaxChoices = rawData.max;
            this.blankOption = null;
            this.ordinal = 0;
            Question.__super__.constructor.call(this, rawData.id);
        }

        Question.prototype.addOption = function(option) {
            log('adding option ' + option.representation);
            if (!(option instanceof Option)) {
                throw new Error('Bad argument type, need an Option');
            }
            if (option.isBlank) {
                if (this.blankOption) {
                    throw new Error('Question already has a blank option');
                }
                this.blankOption = option;
            }
            if (!option.isBlank) {
                this.options.push(option);
                option.ordinal = this.options.length;
            }
            option.parent = this;
            return this;
        };

        return Question;

    })(BallotItem);

    const Candidate = (function(_super) {
        _extends(Candidate, _super);

        Candidate.IS_WRITEIN = 0x1;

        Candidate.IS_BLANK = 0x2;

        function Candidate(rawData, settings) {
            if (settings == null) {
                settings = 0;
            }
            this.id = rawData.id;
            this.allIds = rawData.allIds;
            this.attribute = rawData.attribute;
            this.prime = rawData.representation;
            this.allRepresentations = rawData.allRepresentations;
            this.isBlank = ((settings & this.constructor.IS_BLANK) === this.constructor.IS_BLANK) || false;
            this.isWriteIn = ((settings & this.constructor.IS_WRITEIN) === this.constructor.IS_WRITEIN) || false;
            this.idNumber = '';
            this.name = '';
            this.chosen = 0;
            this.ordinal = 0;
            this.alias = rawData.alias;
            Candidate.__super__.constructor.call(this, rawData.id);
        }

        return Candidate;

    })(BallotItem);

    const List = (function(_super) {
        _extends(List, _super);

        List.IS_WRITEIN = 0x1;

        List.IS_BLANK = 0x2;

        function List(rawList, attribute, settings) {
            if (settings == null) {
                settings = 0;
            }
            this.candidates = [];
            this.id = rawList.id;
            this.attribute = attribute;
            this.idNumber = '';
            this.name = '';
            this.description = '';
            this.chosen = false;
            this.isBlank = ((settings & this.constructor.IS_BLANK) === this.constructor.IS_BLANK) || false;
            this.isWriteIn = ((settings & this.constructor.IS_WRITEIN) === this.constructor.IS_WRITEIN) || false;
            this.blankId = null;
            this.ordinal = 0;
            this.prime = rawList.representation;
            List.__super__.constructor.call(this, rawList.id);
        }

        List.prototype.addCandidate = function(candidate) {
            log('adding candidate ' + candidate.prime);
            if (!(candidate instanceof Candidate)) {
                throw new Error('Bad argument type, need a Candidate');
            }
            this.candidates.push(candidate);
            candidate.ordinal = this.candidates.length;
            candidate.parent = this;
            return this;
        };

        List.prototype.setBlank = function(blankId) {
            if (this.blankId) {
                throw new Error('List already has a blank attribute');
            }
            this.isBlank = true;
            this.blankId = blankId;
            return this;
        };

        return List;

    })(BallotItem);

    const Contest = (function(_super) {
        _extends(Contest, _super);

        function Contest(rawData) {
            this.template = rawData.template;
            Contest.__super__.constructor.call(this, rawData.id);
        }

        return Contest;

    })(BallotItem);

    const Options = (function(_super) {
        _extends(Options, _super);

        function Options(rawData) {
            this.questions = [];
            Options.__super__.constructor.call(this, rawData);
        }

        Options.prototype.addQuestion = function(question) {
            log('adding question ' + question.prime);
            if (!(question instanceof Question)) {
                throw new Error('Bad argument type, need a Question');
            }
            this.questions.push(question);
            question.ordinal = this.questions.length;
            question.parent = this;
            return this;
        };

        return Options;

    })(Contest);

    const ListsAndCandidates = (function(_super) {
        _extends(ListsAndCandidates, _super);

        function ListsAndCandidates(rawData) {
            this.allowFullBlank = rawData.fullBlank === 'true';
            this.lists = [];
            this.listQuestion = {
                minChoices: 0,
                maxChoices: 0,
                cumul: 1
            };
            this.candidatesQuestion = {
                minChoices: 0,
                maxChoices: 0,
                hasWriteIns: false,
                fusions: [],
                cumul: 1
            };
            ListsAndCandidates.__super__.constructor.call(this, rawData);
        }

        ListsAndCandidates.prototype.addList = function(list) {
            log('adding list ' + list.prime);
            if (!(list instanceof List)) {
                throw new Error('Bad argument type, need a List');
            }
            this.lists.push(list);
            list.ordinal = this.lists.length;
            list.parent = this;
            return this;
        };

        return ListsAndCandidates;

    })(Contest);

    const Ballot = (function(_super) {
        _extends(Ballot, _super);

        function Ballot(id) {
            this.contests = [];
            Ballot.__super__.constructor.apply(this, arguments);
        }

        Ballot.prototype.addContest = function(contest) {
            if (!(contest instanceof Contest)) {
                throw new Error('Bad argument type, need a Contest');
            }
            this.contests.push(contest);
            contest.parent = this;
            return this;
        };

        return Ballot;

    })(BallotItem);

    module.exports = {
        Option: Option,
        Question: Question,
        Candidate: Candidate,
        List: List,
        Options: Options,
        ListsAndCandidates: ListsAndCandidates,
        Ballot: Ballot
    };

}).call(this);
